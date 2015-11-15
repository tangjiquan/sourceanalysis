/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import junit.framework.TestCase;

/**
 * Test class for the AbstractCacheAdministrator class. It tests some of the
 * public methods of the admin. Some others cannot be tested since they are
 * linked to the property file used for the tests, and since this file
 * will change, the value of some parameters cannot be asserted
 *
 * $Id: TestAbstractCacheAdministrator.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public abstract class TestAbstractCacheAdministrator extends TestCase {
    // Constants used in the tests
    //private final String CACHE_PATH_PROP = "cache.path";
    //private final String CONTENT = "Content for the abstract cache admin test";
    //private final String ENTRY_KEY = "Test Abstract Admin Key";
    private final String INVALID_PROP_NAME = "INVALID_PROP_NAME";
    //private final String TEST_LOG = "test log";

    /**
     * Constructor for the this test class.
     * <p>
     * @param str Test name (required by JUnit)
     */
    protected TestAbstractCacheAdministrator(String str) {
        super(str);
    }

    /**
     * Cannot be tested since CacheContents is an interface
     */
    public void testCacheContents() {
    }

    /**
     * We cannot test this method because the value depends on the property
     */
    public void testGetCachePath() {
    }

    /**
     * Validate that the properties retrieved by the admin are the same as the one
     * specified in the property file. Do not test cache path or memory cache
     * since it changes with the tests
     */
    public void testGetProperty() {
        // Check if all the default properties are OK
        assertNull(getAdmin().getProperty(INVALID_PROP_NAME));
        assertNull(getAdmin().getProperty(""));

        try {
            assertNull(getAdmin().getProperty(null));
            fail("NullPointerException expected (property Key null).");
        } catch (Exception e) {
        }
    }

    /**
     * We cannot test this method because the value depends on the property
     */
    public void testIsFileCaching() {
    }

    /**
     * We cannot test this method because the value depends on the property
     */
    public void testIsMemoryCaching() {
    }

    /**
     * Perform a call to the log method. Unfornately, there is no way to check
     * if the logging is done correctly, we only invoke it
     */
    public void testLog() {
        // Invoke the log
        // The other log method is not tested since it calls the same as we do
        //TODO

        /*getAdmin().log(TEST_LOG, System.out);
        getAdmin().log("", System.out);
        getAdmin().log(null, System.out);
        getAdmin().log(TEST_LOG, null);
          */
    }

    // Abstract method that returns an instance of an admin
    protected abstract AbstractCacheAdministrator getAdmin();
}
