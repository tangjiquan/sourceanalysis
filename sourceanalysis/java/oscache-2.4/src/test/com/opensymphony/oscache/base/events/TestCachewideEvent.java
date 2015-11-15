/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import java.util.Date;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This is the test class for the CachewideEvent class. It checks that the
 * public methods are working properly
 *
 * $Id: TestCacheEntryEvent.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author Lars Torunski
 */
public final class TestCachewideEvent extends TestCase {

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCachewideEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestCachewideEvent.class);
    }

    /**
     * Test the CacheEntryEvent class
     */
    public void testCacheEntryEvent() {
        // Create all the required objects
        GeneralCacheAdministrator admin = new GeneralCacheAdministrator();

        Date date = new Date();
        CachewideEvent event = new CachewideEvent(admin.getCache(), date, null);
                
        // Get back the values and assert them
        assertEquals(event.getDate(), date);
        assertEquals(event.getCache(), admin.getCache());
    }
}
