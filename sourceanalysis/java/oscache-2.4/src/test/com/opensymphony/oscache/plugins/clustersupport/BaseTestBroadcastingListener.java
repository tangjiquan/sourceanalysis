/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.*;

import junit.framework.TestCase;

import java.util.Date;

/**
 * A base class that provides the framework for testing a cluster listener
 * implementation.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public abstract class BaseTestBroadcastingListener extends TestCase {
    /**
     * The persistance listener used for the tests
     */
    protected static AbstractBroadcastingListener listener = null;

    /**
     * A cache instance to use for the tests
     */
    protected static Cache cache = null;

    /**
     * The number of tests in this class. This is used to keep
     * track of how many tests remain; once we reach zero we shut
     * down the broadcasting listener.
     */
    int testsRemaining = 0;

    /**
     * Cache group
     */
    private final String GROUP = "test group";

    /**
     * Object key
     */
    private final String KEY = "Test clustersupport persistence listener key";

    public BaseTestBroadcastingListener(String str) {
        super(str);
    }

    /**
     * Tests the listener by causing the cache to fire off all its
     * events
     */
    public void testListener() {
        CacheEntry entry = new CacheEntry(KEY, null);

        cache.putInCache(KEY, entry);
        cache.putInCache(KEY, entry, new String[] {GROUP});
        cache.flushEntry(KEY);
        cache.flushGroup(GROUP);
        cache.flushAll(new Date());

        // Note that the remove event is not called since it's not exposed.
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set up the broadcasting listener required for each test.
     */
    public void setUp() {
        // At first invocation, create a listener
        if (listener == null) {
            testsRemaining = countTestCases(); // This seems to always return 1 even if there are multiple tests?

            listener = getListener();
            assertNotNull(listener);

            cache = new Cache(true, false, false);
            assertNotNull(cache);

            try {
                listener.initialize(cache, getConfig());
            } catch (InitializationException e) {
                fail(e.getMessage());
            }

            cache.addCacheEventListener(listener);
        }
    }

    /**
     * Once all the tests are complete this will shut down the broadcasting listener.
     */
    protected void tearDown() throws Exception {
        if (--testsRemaining == 0) {
            try {
                listener.finialize();
                listener = null;
            } catch (FinalizationException e) {
                fail(e.getMessage());
            }
        }
    }

    /**
     * Child classes implement this to return the broadcasting listener instance
     * that will be tested.
     */
    abstract AbstractBroadcastingListener getListener();

    /**
     * Child classes implement this to return the configuration for their listener
     * @return
     */
    abstract Config getConfig();
}
