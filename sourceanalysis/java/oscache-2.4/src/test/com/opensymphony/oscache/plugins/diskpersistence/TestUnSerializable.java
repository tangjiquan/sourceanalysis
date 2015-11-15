/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on Mar 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.opensymphony.oscache.plugins.diskpersistence;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

/**
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestUnSerializable extends TestCase {
    final String CACHE_DIRECTORY_PATH = TestDiskPersistenceListener.CACHEDIR + "/application";
    GeneralCacheAdministrator cache;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();

        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("cache.path", TestDiskPersistenceListener.CACHEDIR);
        properties.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
        properties.setProperty("cache.persistence.overflow.only", "true");

        //	        properties.setProperty("cache.memory", "false");
        properties.setProperty("cache.capacity", "2");
        properties.setProperty("cache.unlimited.disk", "false");
        cache = new GeneralCacheAdministrator(properties);
        cache.getCache().getPersistenceListener().clear();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testNotSerializableObject() throws Exception {
        cache.putInCache("1", new UnSerializable());
        cache.putInCache("2", new UnSerializable());
        assertTrue(isDirectoryEmpty(CACHE_DIRECTORY_PATH));
        cache.putInCache("3", new UnSerializable());
        cache.putInCache("4", new UnSerializable());
        assertTrue(isDirectoryEmpty(CACHE_DIRECTORY_PATH));
        cache.flushAll();
    }

    /**
         * @param filePath
         * @return
         */
    private boolean isDirectoryEmpty(String filePath) {
        File file = new File(filePath);
        return !file.exists() || (file.list().length == 0);
    }

    /**
    * This methods returns the name of this test class to JUnit
    * <p>
    * @return The test for this class
    */
    public static Test suite() {
        return new TestSuite(TestUnSerializable.class);
    }

    public static class UnSerializable {
        int asdfasdfasdf = 234;
    }
}
