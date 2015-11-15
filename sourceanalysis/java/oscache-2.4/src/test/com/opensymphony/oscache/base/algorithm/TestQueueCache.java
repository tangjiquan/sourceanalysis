/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.persistence.PersistenceListener;
import com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener;
import com.opensymphony.oscache.plugins.diskpersistence.TestDiskPersistenceListener;

import java.util.Iterator;
import java.util.Properties;

/**
 * Test class for the QueueCache class, which is the base class for FIFO
 * and LIFO algorithm classes. All the public methods of QueueCache are tested
 * here.
 *
 * $Id: TestQueueCache.java 383 2006-09-10 22:00:01Z larst $
 * @version        $Revision: 383 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public abstract class TestQueueCache extends TestAbstractCache {
    /**
     * Entry content
     */
    protected final String CONTENT = "Test Queue Cache content";

    /**
     * Entry key
     */
    protected final String KEY = "Test Queue Cache key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestQueueCache(String str) {
        super(str);
    }

    /**
     * Test the specific algorithms
     */
    public abstract void testRemoveItem();

    /**
     * Test the clear
     */
    public void testClear() {
        getCache().clear();
        assertEquals(0, getCache().size());
    }

    /**
     * Test the ContainsKey method
     */
    public void testContainsKey() {
        getCache().put(KEY, CONTENT);
        assertTrue(getCache().containsKey(KEY));
        getCache().clear();
    }

    /**
     * Test the get method
     */
    public void testGet() {
        // Add an entry and verify that it is there
        getCache().put(KEY, CONTENT);
        assertTrue(getCache().get(KEY).equals(CONTENT));

        // Call with invalid parameters
        try {
            getCache().get(null);
            fail("Get called with null parameters!");
        } catch (Exception e) { /* This is what we expect */
        }

        getCache().clear();
    }

    /**
     * Test the getter and setter for the max entries
     */
    public void testGetSetMaxEntries() {
        // Check that the cache is full, then chop it by one and assert that
        // an element has been removed
        for (int count = 0; count < MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        assertEquals(MAX_ENTRIES, getCache().size());
        getCache().setMaxEntries(MAX_ENTRIES - 1);
        assertEquals(MAX_ENTRIES - 1, getCache().getMaxEntries());
        assertEquals(MAX_ENTRIES - 1, getCache().size());

        // Specify an invalid capacity
        try {
            getCache().setMaxEntries(INVALID_MAX_ENTRIES);
            fail("Cache capacity set with an invalid argument");
        } catch (Exception e) {
            // This is what we expect
        }

        getCache().clear();
    }

    /**
     * Test the iterator
     */
    public void testIterator() {
        // Verify that the iterator returns MAX_ENTRIES and no more elements
        int nbEntries = getCache().size();
        Iterator iterator = getCache().entrySet().iterator();
        assertNotNull(iterator);

        for (int count = 0; count < nbEntries; count++) {
            assertNotNull(iterator.next());
        }

        assertTrue(!iterator.hasNext());
    }

    /**
     * Test the put method
     */
    public void testPut() {
        // Put elements in cache
        for (int count = 0; count < MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        // Call with invalid parameters
        try {
            getCache().put(null, null);
            fail("Put called with null parameters!");
        } catch (Exception e) { /* This is what we expect */
        }

        getCache().clear();
    }

    /**
     * Test the put method with overflow parameter set
     */
    public void testPutOverflow() {
        // Create a listener
        PersistenceListener listener = new DiskPersistenceListener();

        Properties p = new Properties();
        p.setProperty("cache.path", TestDiskPersistenceListener.CACHEDIR);
        p.setProperty("cache.memory", "true");
        p.setProperty("cache.persistence.overflow.only", "true");
        p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
        listener.configure(new Config(p));
        getCache().setPersistenceListener(listener);
        getCache().clear();
        getCache().setMaxEntries(MAX_ENTRIES);
        getCache().setOverflowPersistence(true);

        if (getCache() instanceof UnlimitedCache) {
            return; // nothing to test since memory will never overflow.
        }

        // Put elements in cache
        for (int count = 0; count <= MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        try {
            int numPersisted = 0;

            // Check that number of elements persisted == 1 if it is an overflow cache or all
            // if it is not overflow and writes every time.
            for (int count = 0; count <= MAX_ENTRIES; count++) {
                if (getCache().getPersistenceListener().isStored(KEY + count)) {
                    numPersisted++;
                }
            }

            if (getCache().isOverflowPersistence()) {
                assertTrue("Only 1 element should have been persisted ", numPersisted == 1);
            } else {
                assertTrue("All elements should have been persisted ", numPersisted == (MAX_ENTRIES + 1));
            }
        } catch (Exception e) {
            fail();
        }

        getCache().clear();
    }
  
    /**
     * Test if bug CACHE-255 disappeared.
     */
    public void testBugCache255() {
        if (!getCache().isMemoryCaching()) {
            return; // nothing to test since memory won't be used.
        }
        if (getCache() instanceof UnlimitedCache) {
            return; // nothing to test since memory will never overflow.
        }

        // fill up the cache
        for (int count = 0; count < MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        // get the old value
        Object oldValue = getCache().put(KEY + MAX_ENTRIES, CONTENT + MAX_ENTRIES);

        assertEquals("Evicted object content should be the same", CONTENT + "0", oldValue);

        getCache().clear();
    }

    /**
     * Test the remove from cache
     */
    public void testRemove() {
        getCache().put(KEY, CONTENT);

        // Remove the object and assert the return
        assertNotNull(getCache().remove(KEY));
        getCache().clear();
    }
}
