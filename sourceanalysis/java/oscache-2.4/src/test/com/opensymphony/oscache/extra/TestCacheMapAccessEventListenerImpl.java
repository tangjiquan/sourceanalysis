/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheMapAccessEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEventType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the cache map access event listener implementation
 *
 * $Id: TestCacheMapAccessEventListenerImpl.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestCacheMapAccessEventListenerImpl extends TestCase {
    /**
     * Key used for this test
     */
    private final String KEY = "Test Cache Map Access Event Listener Impl Key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheMapAccessEventListenerImpl(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheMapAccessEventListenerImpl.class);
    }

    /**
     * Test the basic implementation of the listener
     */
    public void testCacheMapAccessEventListenerImpl() {
        // Build objects required for the tests
        CacheEntry entry = new CacheEntry(KEY);
        CacheMapAccessEventListenerImpl listener = new CacheMapAccessEventListenerImpl();

        // Genereate events
        listener.accessed(new CacheMapAccessEvent(CacheMapAccessEventType.HIT, entry));
        listener.accessed(new CacheMapAccessEvent(CacheMapAccessEventType.HIT, entry));
        listener.accessed(new CacheMapAccessEvent(CacheMapAccessEventType.STALE_HIT, entry));
        listener.accessed(new CacheMapAccessEvent(CacheMapAccessEventType.MISS, entry));

        // Assert the counters
        assertEquals(listener.getHitCount(), 2);
        assertEquals(listener.getStaleHitCount(), 1);
        assertEquals(listener.getMissCount(), 1);

        // Reset the counts
        listener.reset();
        assertEquals(listener.getHitCount(), 0);
        assertEquals(listener.getStaleHitCount(), 0);
        assertEquals(listener.getMissCount(), 0);
    }
}
