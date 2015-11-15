/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.diskpersistence;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.persistence.CachePersistenceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FilenameFilter;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Test all the public methods of the disk persistance listener and assert the
 * return values
 *
 * $Id: TestDiskPersistenceListener.java 422 2007-03-17 23:47:29Z larst $
 * @version        $Revision: 422 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestDiskPersistenceListener extends TestCase {
    /**
     * Cache dir to persist to
     */
    public static final String CACHEDIR = "/tmp/diskcache";

    /**
     * The persistance listener used for the tests
     */
    private DiskPersistenceListener listener = null;

    /**
     * Object content
     */
    private final String CONTENT = "Disk persistance content";

    /**
     * Cache group
     */
    private final String GROUP = "test group";

    /**
     * Object key
     */
    private final String KEY = "Test disk persistance listener key";
    private CacheFileFilter cacheFileFilter = new CacheFileFilter();

    public TestDiskPersistenceListener(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestDiskPersistenceListener.class);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a listener
        listener = new DiskPersistenceListener();

        Properties p = new Properties();
        p.setProperty("cache.path", CACHEDIR);
        p.setProperty("cache.memory", "false");
        p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
        listener.configure(new Config(p));
    }

    /**
     * Test the cache directory removal
     */
    public void testClear() {
        // Create an new element since we removed it at the last test
        testStoreRetrieve();

        // Remove the directory, and assert that we have no more entry
        try {
            listener.clear();
            assertTrue(!listener.isStored(KEY));
        } catch (CachePersistenceException cpe) {
            cpe.printStackTrace();
            fail("Exception thrown in test clear!");
        }
    }

    /**
     * Test that the previouly created file exists
     */
    public void testIsStored() {
        try {
            listener.store(KEY, CONTENT);

            // Retrieve the previously created file
            assertTrue(listener.isStored(KEY));

            // Check that the fake key returns false
            assertTrue(!listener.isStored(KEY + "fake"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("testIsStored raised an exception");
        }
    }

    /**
     * Test the cache removal
     */
    public void testRemove() {
        // Create an entry if it doesn't exists
        try {
            if (!listener.isStored(KEY)) {
                listener.store(KEY, CONTENT);
            }

            // Remove the previously created file
            listener.remove(KEY);
        } catch (CachePersistenceException cpe) {
            cpe.printStackTrace();
            fail("Exception thrown in test remove!");
        }
    }

    /**
     * Force CachePersistenceException to get a 100% in the unit test
     */
    public void testCachePersistenceException() {
        try {
            for (int i = 0; i < 2; i++) {
                if (i == 1) throw new CachePersistenceException("test");             
            }
            fail("CachePersistenceException not thrown!");
        } catch (CachePersistenceException cpe) {
            // ignore
        }
        try {
            for (int i = 0; i < 2; i++) {
                if (i == 1) throw new CachePersistenceException();             
            }
            fail("CachePersistenceException not thrown!");
        } catch (CachePersistenceException cpe) {
            // ignore
        }
    }
    
    /**
     * Test the disk store and retrieve
     */
    public void testStoreRetrieve() {
        // Create a cache entry and store it
        CacheEntry entry = new CacheEntry(KEY);
        entry.setContent(CONTENT);

        try {
            listener.store(KEY, entry);

            // Retrieve our entry and validate the values
            CacheEntry newEntry = (CacheEntry) listener.retrieve(KEY);
            assertTrue(entry.getContent().equals(newEntry.getContent()));
            assertEquals(entry.getCreated(), newEntry.getCreated());
            assertTrue(entry.getKey().equals(newEntry.getKey()));

            // Try to retrieve a non-existent object
            assertNull(listener.retrieve("doesn't exist"));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!");
        }
    }

    /**
     * Test the storing and retrieving of groups
     */
    public void testStoreRetrieveGroups() {
        // Store a group
        Set groupSet = new HashSet();
        groupSet.add("1");
        groupSet.add("2");

        try {
            listener.storeGroup(GROUP, groupSet);

            // Retrieve it and validate its contents
            groupSet = listener.retrieveGroup(GROUP);
            assertNotNull(groupSet);

            assertTrue(groupSet.contains("1"));
            assertTrue(groupSet.contains("2"));
            assertFalse(groupSet.contains("3"));

            // Try to retrieve a non-existent group
            assertNull(listener.retrieveGroup("abc"));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!");
        }
    }

    protected void tearDown() throws Exception {
        listener.clear();
        assertTrue("Cache not cleared", new File(CACHEDIR).list(cacheFileFilter).length == 0);
    }

    private static class CacheFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return !"__groups__".equals(name);
        }
    }
}
