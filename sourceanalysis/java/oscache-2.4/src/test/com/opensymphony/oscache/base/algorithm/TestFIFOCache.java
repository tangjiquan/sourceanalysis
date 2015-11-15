/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the FIFOCache class. It tests that the algorithm reacts as
 * expected when entries are removed
 *
 * $Id: TestFIFOCache.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestFIFOCache extends TestQueueCache {
    /**
     * FIFO Cache object
     */
    private static FIFOCache cache = null;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestFIFOCache(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestFIFOCache.class);
    }

    /**
     * Abstract method used by the TestAbstractCache class
     * <p>
     * @return  A cache instance
     */
    public AbstractConcurrentReadCache getCache() {
        return cache;
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // Create a cache instance on first invocation
        if (cache == null) {
            cache = new FIFOCache();
            assertNotNull(cache);
        }
    }

    /**
     * Test the cache algorithm
     */
    public void testRemoveItem() {
        // Add 2 elements in the cache and ensure that the one to remove is the first
        // inserted
        cache.itemPut(KEY);
        cache.itemPut(KEY + 1);
        assertTrue(KEY.equals(cache.removeItem()));
    }
}
