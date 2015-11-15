/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.persistence.CachePersistenceException;
import com.opensymphony.oscache.base.persistence.PersistenceListener;

import junit.framework.TestCase;

/**
 * Test class for the AbstractCache class. It tests all public methods of
 * the AbstractCache and assert the results. It is design to run under JUnit.
 *
 * $Id: TestAbstractCache.java 425 2007-03-18 09:45:03Z larst $
 * @version        $Revision: 425 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public abstract class TestAbstractCache extends TestCase {
    /**
     * Invalid cache capacity
     */
    protected final int INVALID_MAX_ENTRIES = 0;

    /**
     * Cache capacity
     */
    protected final int MAX_ENTRIES = 3;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    protected TestAbstractCache(String str) {
        super(str);
    }

    /**
     * Test the method that verify if the cache contains a specific key
     */
    public abstract void testContainsKey();

    /**
     * Test the get from the cache
     */
    public abstract void testGet();

    /**
     * Test the capacity setting
     */
    public void testGetSetMaxEntries() {
        getCache().setMaxEntries(MAX_ENTRIES);
        assertEquals(MAX_ENTRIES, getCache().getMaxEntries());

        // Specify an invalid capacity
        try {
            getCache().setMaxEntries(INVALID_MAX_ENTRIES);
            fail("Cache capacity set with an invalid argument");
        } catch (Exception e) {
            // This is what we expect
        }
    }

    /**
     * Test the setting of the memory cache
     */
    public void testGetSetMemoryCache() {
        getCache().setMemoryCaching(true);
        assertTrue(getCache().isMemoryCaching());
    }

    /**
     * Test the iterator retrieval
     */
    public abstract void testIterator();

    /**
     * Test the put into the cache
     */
    public abstract void testPut();

    /**
     * Test the remove from the cache
     */
    public abstract void testRemove();

    /**
     * Test the specific details about the cache algorithm
     */
    public abstract void testRemoveItem();

    /**
     * Test the PersistenceListener setter. Since the persistance listener is
     * an interface, just call the setter with null
     */
    public void testSetPersistenceListener() {
        getCache().setPersistenceListener(null);
    }

    // Abstract method that returns an instance of an admin
    protected abstract AbstractConcurrentReadCache getCache();

    /**
     * Test that groups are correctly updated on puts and removes
     * See CACHE-188 and maybe CACHE-244
     */
    public void testGroups() {
      String KEY = "testkey";
      String KEY2 = "testkey2";
      String GROUP_NAME = "group1";
      CacheEntry entry = new CacheEntry(KEY, null);
      entry.setContent("testvalue");
      entry.setGroups(new String[] {GROUP_NAME});
      getCache().put(KEY, entry);

      Map m = getCache().getGroupsForReading();
      assertNotNull("group must exist", m.get(GROUP_NAME));
      try {
        Set group = (Set)m.get(GROUP_NAME);
        assertEquals(1, group.size());
        Object keyFromGroup = group.iterator().next();
        assertEquals(KEY, keyFromGroup);
      } catch (ClassCastException e) {
        fail("group should have been a java.util.Set but is a " +
            m.get(GROUP_NAME).getClass().getName());
      }

      assertNotNull(getCache().remove(KEY));

      m = getCache().getGroupsForReading();
      assertNull("group should have been deleted (see CACHE-188)", m.get(GROUP_NAME));
      getCache().clear();

      // Test if persistence options are correctly considered for groups
      try {
        PersistenceListener listener = new MockPersistenceListener();
        getCache().setPersistenceListener(listener);
        getCache().setOverflowPersistence(false);
        getCache().put(KEY, entry);
        assertTrue(listener.isStored(KEY));
        Set group = listener.retrieveGroup(GROUP_NAME);
        assertNotNull(group);
        assertTrue(group.contains(KEY));

        getCache().remove(KEY);
        assertFalse(listener.isStored(KEY));
        getCache().clear();

        // test overflow persistence
        getCache().setOverflowPersistence(true);
        getCache().setMaxEntries(1);
        getCache().put(KEY, entry);
        assertFalse(listener.isStored(KEY));
        // is it correct that the group is persisted, even when we use overflow only?
        // assertFalse(listener.isGroupStored(GROUP_NAME));

        CacheEntry entry2 = new CacheEntry(KEY2);
        entry2.setContent("testvalue");
        entry2.setGroups(new String[] {GROUP_NAME});
        getCache().put(KEY2, entry2);
        // oldest must have been persisted to disk:
        assertTrue(listener.isStored(KEY));
        assertFalse(listener.isStored(KEY2));
        assertNotNull(getCache().get(KEY2));
      } catch (CachePersistenceException e) {
        e.printStackTrace();
        fail("Excpetion was thrown");
      }
    }
    
    public void testMisc() {
        getCache().clear();
        assertTrue(getCache().capacity() > 0);

        final String KEY = "testkeymisc";
        final String CONTENT = "testkeymisc";

        CacheEntry entry = new CacheEntry(KEY, null);
        entry.setContent(CONTENT);
        
        if (getCache().contains(entry) == false) {
            getCache().put(KEY, entry);
        }
        assertTrue(getCache().contains(entry));
        
        CacheEntry entry2 = new CacheEntry(KEY+"2", null);
        entry.setContent(CONTENT+"2");
        getCache().put(entry2.getKey(), entry2);

        Enumeration enumeration = getCache().elements();
        assertTrue(enumeration.hasMoreElements());
        while (enumeration.hasMoreElements()) enumeration.nextElement();
    }


    private static class MockPersistenceListener implements PersistenceListener {
      
      private Map entries = new HashMap();
      private Map groups = new HashMap();

      public void clear() throws CachePersistenceException {
        entries.clear();
        groups.clear();
      }

      public PersistenceListener configure(Config config) {
        return this;
      }

      public boolean isGroupStored(String groupName) throws CachePersistenceException {
        return groups.containsKey(groupName);
      }

      public boolean isStored(String key) throws CachePersistenceException {
        return entries.containsKey(key);
      }

      public void remove(String key) throws CachePersistenceException {
        entries.remove(key);
      }

      public void removeGroup(String groupName) throws CachePersistenceException {
        groups.remove(groupName);
      }

      public Object retrieve(String key) throws CachePersistenceException {
        return entries.get(key);
      }

      public Set retrieveGroup(String groupName) throws CachePersistenceException {
        return (Set)groups.get(groupName);
      }

      public void store(String key, Object obj) throws CachePersistenceException {
        entries.put(key, obj);
      }

      public void storeGroup(String groupName, Set group) throws CachePersistenceException {
        groups.put(groupName, group);
      }
    }
}
