/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This is the test class for the CachePatternEvent class. It checks that the
 * public methods are working properly
 *
 * $Id: TestCacheEntryEvent.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author Lars Torunski
 */
public final class TestCachePatternEvent extends TestCase {

    /**
     * Constants required for the test
     */
    private final String TEST_PATTERN = "testPattern";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCachePatternEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestCachePatternEvent.class);
    }

    /**
     * Test the CacheEntryEvent class
     */
    public void testCacheEntryEvent() {
        // Create all the required objects
        GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
        Cache map = new Cache(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence());

        // three parameters
        CachePatternEvent event = new CachePatternEvent(map, TEST_PATTERN, null);

        // Get back the values and assert them
        assertEquals(event.getMap(), map);
        assertEquals(event.getPattern(), TEST_PATTERN);
        assertNull(event.getOrigin());
        
        // two parameters
        CachePatternEvent event2 = new CachePatternEvent(map, TEST_PATTERN);

        // Get back the values and assert them
        assertEquals(event2.getMap(), map);
        assertEquals(event.getPattern(), TEST_PATTERN);
        assertNull(event2.getOrigin());
    }
}
