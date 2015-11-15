/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the LRUCache class. It only tests that the algorithm reacts as
 * expected when entries are removed. All the other tests related to the LRU
 * algorithm are in the TestNonQueueCache class, since those tests are shared
 * with the TestUnlimitedCache class.
 *
 * $Id: TestLRUCache.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestLRUCache extends TestQueueCache {
    /**
     * LRU Cache object
     */
    private static LRUCache cache = null;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestLRUCache(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestLRUCache.class);
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
            cache = new LRUCache();
            assertNotNull(cache);
        }
    }

    /**
     * Test the cache algorithm
     */
    public void testRemoveItem() {
        // Add 3 elements
        cache.itemPut(KEY);
        cache.itemPut(KEY + 1);
        cache.itemPut(KEY + 2);

        // Get the last element
        cache.itemRetrieved(KEY);

        // The least recently used item is key + 1
        assertTrue((KEY + 1).equals(cache.removeItem()));
    }
}
