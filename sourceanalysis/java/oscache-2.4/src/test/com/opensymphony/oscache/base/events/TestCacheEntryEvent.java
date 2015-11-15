/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This is the test class for the CacheEntryEvent class. It checks that the
 * public methods are working properly
 *
 * $Id: TestCacheEntryEvent.java 419 2007-03-17 13:01:19Z larst $
 * @version        $Revision: 419 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCacheEntryEvent extends TestCase {
    /**
     * Constants required for the test
     */
    private final String KEY = "Test cache entry event key";
    private final String KEY_2 = "Test cache entry event key 2";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheEntryEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheEntryEvent.class);
    }

    /**
     * Test the CacheEntryEvent class
     */
    public void testCacheEntryEvent() {
        // Create all the required objects
        GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
        Cache map = new Cache(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence());

        // test with key
        CacheEntry entry = new CacheEntry(KEY);
        CacheEntryEvent event = new CacheEntryEvent(map, entry, null);

        // Get back the values and assert them
        assertEquals(event.getEntry(), entry);
        assertEquals(event.getKey(), KEY);
        assertEquals(event.getMap(), map);
        assertNull(event.getOrigin());
        
        CacheEntry entry2 = new CacheEntry(KEY_2);
        CacheEntryEvent event2 = new CacheEntryEvent(map, entry2);

        // Get back the values and assert them
        assertEquals(event2.getEntry(), entry2);
        assertEquals(event2.getKey(), KEY_2);
        assertEquals(event2.getMap(), map);
        assertNull(event2.getOrigin());
    }
}
