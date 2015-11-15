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
 * This is the test class for the CacheGroupEvent class. It checks that the
 * public methods are working properly
 *
 * $Id: TestCacheEntryEvent.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author Lars Torunski
 */
public final class TestCacheGroupEvent extends TestCase {

    /**
     * Constants required for the test
     */
    private final String TEST_GROUP = "testGroup";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheGroupEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheGroupEvent.class);
    }

    /**
     * Test the CacheEntryEvent class
     */
    public void testCacheEntryEvent() {
        // Create all the required objects
        GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
        Cache map = new Cache(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence());

        // three parameters
        CacheGroupEvent event = new CacheGroupEvent(map, TEST_GROUP, null);

        // Get back the values and assert them
        assertEquals(event.getMap(), map);
        assertEquals(event.getGroup(), TEST_GROUP);
        assertNull(event.getOrigin());
        
        // two parameters
        CachePatternEvent event2 = new CachePatternEvent(map, TEST_GROUP);

        // Get back the values and assert them
        assertEquals(event2.getMap(), map);
        assertEquals(event.getGroup(), TEST_GROUP);
        assertNull(event2.getOrigin());
    }
}
