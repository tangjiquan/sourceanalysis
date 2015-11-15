/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.CacheEntry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This is the test class for the CacheMapAccessEvent class. It checks that the
 * public methods are working properly
 *
 * $Id: TestCacheMapAccessEvent.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCacheMapAccessEvent extends TestCase {
    private final String KEY = "Test cache map access event key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheMapAccessEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheMapAccessEvent.class);
    }

    /**
     * Test the CacheMapAccessEvent class
     */
    public void testCacheMapAccessEvent() {
        // Create an object and check the parameters
        CacheEntry entry = new CacheEntry(KEY);
        CacheMapAccessEvent event = new CacheMapAccessEvent(CacheMapAccessEventType.HIT, entry);
        assertEquals(event.getCacheEntry(), entry);
        assertEquals(event.getCacheEntryKey(), KEY);
        assertEquals(event.getEventType(), CacheMapAccessEventType.HIT);
    }
}
