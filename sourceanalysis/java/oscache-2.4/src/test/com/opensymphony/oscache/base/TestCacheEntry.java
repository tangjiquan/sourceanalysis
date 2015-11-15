/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the public methods of the CacheEntry class
 *
 * $Id: TestCacheEntry.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestCacheEntry extends TestCase {
    // Static variables required thru the tests
    static CacheEntry entry = null;
    static long beforeCreation = 0;
    static long afterCreation = 0;
    private final String CONTENT = "Content for the cache entry test";

    // Constants used thru the tests
    private final String ENTRY_KEY = "Test cache entry key";
    private final int NO_REFRESH_NEEDED = 1000000;
    private final int REFRESH_NEEDED = 0;

    /**
     * Class constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheEntry(String str) {
        super(str);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a cache entry object
        if (entry == null) {
            // Log the time before and after to verify the creation time
            // in one of the tests
            beforeCreation = System.currentTimeMillis();

            entry = new CacheEntry(ENTRY_KEY);
            afterCreation = System.currentTimeMillis();
        }
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheEntry.class);
    }

    /**
     * Verify the flush
     */
    public void testFlush() {
        // Set the content so it shouldn't need refresh
        entry.setContent(CONTENT);
        assertTrue(!entry.needsRefresh(NO_REFRESH_NEEDED));

        // Flush the entry. It should now needs refresh
        entry.flush();
        assertTrue(entry.needsRefresh(NO_REFRESH_NEEDED));
    }

    /**
     * Verify that the creation time is correct
     */
    public void testGetCreated() {
        assertBetweenOrEquals(beforeCreation, entry.getCreated(), afterCreation);
    }

    /**
     * Retrieve the item created by the setup
     */
    public void testGetKey() {
        assertTrue(entry.getKey().equals(ENTRY_KEY));
    }

    /**
     * Verify that the last modification time is between the time before and
     * after the alteration of the item
     */
    public void testGetLastUpdate() {
        // again. Then we ensure that the update time is between our timestamps
        long before = System.currentTimeMillis();
        entry.setContent(CONTENT);

        long after = System.currentTimeMillis();
        assertBetweenOrEquals(before, entry.getLastUpdate(), after);
    }

    /**
     * Verify that the "freshness detection" function properly
     */
    public void testNeedsRefresh() {
        // Set the entry content so it shouldn't need refresh
        // Invoke needsRefresh with no delay, so it should return true.
        // Then invoke it with a big delay, so it should return false
        assertTrue(entry.needsRefresh(REFRESH_NEEDED));
        assertTrue(!entry.needsRefresh(NO_REFRESH_NEEDED));
    }

    /**
     * Set the content of the item created by setup and then retrieve it and
     * validate it
     */
    public void testSetGetContent() {
        entry.setContent(CONTENT);
        assertTrue(CONTENT.equals(entry.getContent()));

        // Ensure that nulls are allowed
        entry.setContent(null);
        assertNull(entry.getContent());
    }

    /**
     * Ensure that a value is between two others. Since the execution may be
     * very fast, equals values are also considered to be between
     */
    private void assertBetweenOrEquals(long first, long between, long last) {
        assertTrue(between >= first);
        assertTrue(between <= last);
    }
}
