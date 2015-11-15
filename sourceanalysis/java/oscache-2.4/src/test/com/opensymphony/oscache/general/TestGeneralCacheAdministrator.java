/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.general;

import java.util.Date;

import com.opensymphony.oscache.base.*;
import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all the public methods of the GeneralCacheAdministrator class. Since
 * this class extends the TestAbstractCacheAdministrator class, the
 * AbstractCacheAdministrator is tested when invoking this class.
 *
 * $Id: TestGeneralCacheAdministrator.java 425 2007-03-18 09:45:03Z larst $
 * @version        $Revision: 425 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestGeneralCacheAdministrator extends TestAbstractCacheAdministrator {
    // Constants used thru all the tests
    private static final String KEY = "Test General Cache Admin Key";
    private static final int NO_REFRESH_NEEDED = CacheEntry.INDEFINITE_EXPIRY;
    private static final int REFRESH_NEEDED = 0;
    private static final String CONTENT = "Content for the general cache admin test";
    private static final String WILL_NOT_FLUSH_PATTERN = "This key won't flush";
    private static final String GROUP1 = "group1";
    private static final String GROUP2 = "group2";
    private static final String GROUP3 = "group3";

    // Constants for listener counters
    private static final int NB_CACHE_HITS = 7;
    private static final int NB_CACHE_STALE_HITS = 7;
    private static final int NB_CACHE_MISSED = 1;
    private static final int NB_ADD = 7;
    private static final int NB_UPDATED = 2;
    private static final int NB_FLUSH = 3;
    private static final int NB_REMOVED = 0;
    private static final int NB_GROUP_FLUSH = 2;
    private static final int NB_PATTERN_FLUSH = 1;

    // Static instance of a cache administrator
    static GeneralCacheAdministrator admin = null;

    // Declare the listeners
    private CacheEntryEventListenerImpl cacheEntryEventListener = null;
    private CacheMapAccessEventListenerImpl cacheMapAccessEventListener = null;

    /**
     * Class constructor
     * <p>
     * @param str Test name (required by JUnit)
     */
    public TestGeneralCacheAdministrator(String str) {
        super(str);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        return new TestSuite(TestGeneralCacheAdministrator.class);
    }

    /**
     * Abstract method used by the TestAbstractCacheAdministrator class
     * <p>
     * @return  An administrator instance
     */
    public AbstractCacheAdministrator getAdmin() {
        return admin;
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a administrator
        admin = new GeneralCacheAdministrator();
        assertNotNull(admin);
        cacheEntryEventListener = new CacheEntryEventListenerImpl();
        cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl();

        // Register the listeners on the cache map
        admin.getCache().addCacheEventListener(cacheEntryEventListener);
        admin.getCache().addCacheEventListener(cacheMapAccessEventListener);
    }

    /**
     * Validate the CacheEntryEventListener's data
     */
    public void testCacheEntryEventListenerCounters() {
        populate();
        assertEquals(NB_ADD, cacheEntryEventListener.getEntryAddedCount());
        assertEquals(NB_REMOVED, cacheEntryEventListener.getEntryRemovedCount());
        assertEquals(NB_UPDATED, cacheEntryEventListener.getEntryUpdatedCount());
        assertEquals(NB_GROUP_FLUSH, cacheEntryEventListener.getGroupFlushedCount());
        assertEquals(NB_PATTERN_FLUSH, cacheEntryEventListener.getPatternFlushedCount());
        assertEquals(NB_FLUSH, cacheEntryEventListener.getEntryFlushedCount());
    }

    /**
     * Validate the CacheEntryEventListener's data
     */
    public void testCacheMapAccessEventListenerCounters() {
        populate();

        int missCount = cacheMapAccessEventListener.getMissCount();

        if (NB_CACHE_MISSED != missCount) {
            fail("We expected " + NB_CACHE_MISSED + " misses but got " + missCount + "." + " This is probably due to existing disk cache, delete it and re-run" + " the test");
        }

        assertEquals(NB_CACHE_HITS, cacheMapAccessEventListener.getHitCount());
        assertEquals(NB_CACHE_STALE_HITS, cacheMapAccessEventListener.getStaleHitCount());
    }

    /**
     * Ensure that item may be flushed by key pattern
     */
    public void testFlushPattern() {
        // Put some content in cache
        admin.putInCache(KEY, CONTENT);

        // Call flush pattern with parameters that must NOT flush our object
        admin.flushPattern(WILL_NOT_FLUSH_PATTERN);
        admin.flushPattern("");
        admin.flushPattern(null);

        // Ensure that our object is not gone
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, false));

        // This time we flush it for real
        admin.flushPattern(KEY.substring(1, 2));
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, true));
    }

    /**
     * Ensure that item may be flushed by the entry itself
     */
    public void testFlushEntry() {
        // Put some content in cache
        admin.putInCache(KEY, CONTENT);

        // Call flush pattern with parameters that must NOT flush our object
        admin.flushEntry(WILL_NOT_FLUSH_PATTERN);

        // Ensure that our object is not gone
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, false));

        // This time we flush it for real
        admin.flushEntry(KEY);
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, true));
    }
    
    /**
     * Ensure that item may be flushed by flush all
     */
    public void testFlushAll() {
        // Put some content in cache
        admin.putInCache(KEY, CONTENT);

        // Ensure that our object is not gone
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, false));

        // This time we flush it for real
        admin.flushAll();
        assertNotNull(checkObj(KEY, NO_REFRESH_NEEDED, true));
    }
    
    /**
     * Ensure that the cache groupings work correctly
     */
    public void testGroups() {
        // Flush a non-existent group - should be OK and will still fire a GROUP_FLUSHED event
        admin.flushGroup(GROUP1);

        // Add some items to various group combinations
        admin.putInCache("1", "item 1"); // No groups
        admin.putInCache("2", "item 2", new String[] {GROUP1}); // Just group 1
        admin.putInCache("3", "item 3", new String[] {GROUP2}); // Just group 2
        admin.putInCache("4", "item 4", new String[] {GROUP1, GROUP2}); // groups 1 & 2
        admin.putInCache("5", "item 5", new String[] {GROUP1, GROUP2, GROUP3}); // groups 1,2 & 3

        admin.flushGroup(GROUP3); // This should flush item 5 only
        assertNotNull(checkObj("5", NO_REFRESH_NEEDED, true));
        assertNotNull(checkObj("4", NO_REFRESH_NEEDED, false));

        admin.flushGroup(GROUP2); // This should flush items 3 and 4
        assertNotNull(checkObj("1", NO_REFRESH_NEEDED, false));
        assertNotNull(checkObj("2", NO_REFRESH_NEEDED, false));
        assertNotNull(checkObj("3", NO_REFRESH_NEEDED, true));
        assertNotNull(checkObj("4", NO_REFRESH_NEEDED, true));

        admin.flushGroup(GROUP1); // Flushes item 2
        assertNotNull(checkObj("1", NO_REFRESH_NEEDED, false));
        assertNotNull(checkObj("2", NO_REFRESH_NEEDED, true));

        // Test if regrouping a cache entry works
        admin.putInCache("A", "ABC", new String[] {"A"});
        admin.putInCache("A", "ABC", new String[] {"A", "B"});
        admin.putInCache("B", "DEF", new String[] {"B"});
        admin.flushGroup("B");
        assertNotNull(checkObj("A", NO_REFRESH_NEEDED, true));
    }

    /**
     * Test the main cache functionalities, which are storing and retrieving objects
     * from it
     */
    public void testPutInCacheAndGetFromCache() {
        // Put some item in cache and get it back right away. It should not need
        // to be refreshed
        admin.putInCache(KEY, CONTENT);

        String cacheContent = (String) checkObj(KEY, NO_REFRESH_NEEDED, false);
        assertTrue(CONTENT.equals(cacheContent));

        // Get the item back again and expect a refresh
        cacheContent = (String) checkObj(KEY, REFRESH_NEEDED, true);
        assertTrue(CONTENT.equals(cacheContent));

        // Call the put in cache with invalid values
        invalidPutInCacheArgument(null, null);
        admin.putInCache(KEY, null); // This will still update the cache - cached items can be null

        // Call the getFromCache with invalid values
        invalidGetFromCacheArgument(null, 0);

        // Try to retrieve the values
        assertNull(checkObj(KEY, NO_REFRESH_NEEDED, false));

        // Try to retrieve an item that is not in the cache
        Object obj = checkObj("Not in cache", NO_REFRESH_NEEDED, true);
        assertNull(obj);
    }

    /**
     * Test the main cache functionalities, which are storing and retrieving objects
     * from it
     */
    public void testPutInCacheAndGetFromCacheWithPolicy() {
        String key = "policy";

        // We put content in the cache and get it back
        admin.putInCache(key, CONTENT, new DummyAlwayRefreshEntryPolicy());

        // Should get a refresh
        try {
            admin.getFromCache(key, -1);
            fail("Should have got a refresh.");
        } catch (NeedsRefreshException nre) {
            admin.cancelUpdate(key);
        }
    }

    protected void tearDown() throws Exception {
        if (admin != null) {
            admin.getCache().removeCacheEventListener(cacheEntryEventListener);
            admin.getCache().removeCacheEventListener(cacheMapAccessEventListener);
        }
    }


    /**
     * Bug CACHE-241
     */
	public void testFlushDateTomorrow() {
		GeneralCacheAdministrator cacheAdmin = new GeneralCacheAdministrator(null);
		
		cacheAdmin.putInCache("key1", "key1value");
		
		try {
			assertNotNull(cacheAdmin.getFromCache("key1"));
		} catch (NeedsRefreshException e1) {
			fail("Previous cache key1 doesn't exsits in GCA for the test!");
		}
		
		cacheAdmin.flushAll(new Date(System.currentTimeMillis() + 5000)); // flush in 5 sec.
		try {
			cacheAdmin.getFromCache("key1"); 
		} catch (NeedsRefreshException e) {
			cacheAdmin.cancelUpdate("key1");
			fail("NRE is thrown, but key will expire in 5s."); // it fails here
		}
	}


    /**
     * Utility method that tries to get an item from the cache and verify
     * if all goes as expected
     * <p>
     * @param key       The item key
     * @param refresh   The timestamp specifiying if the item needs refresh
     * @param exceptionExpected Specify if we expect a NeedsRefreshException
     */
    private Object checkObj(String key, int refresh, boolean exceptionExpected) {
        // Cache content
        Object content = null;

        try {
            // try to find an object
            content = admin.getFromCache(key, refresh);

            if (exceptionExpected) {
                fail("Expected NeedsRefreshException!");
            }
        } catch (NeedsRefreshException nre) {
            admin.cancelUpdate(key);

            if (!exceptionExpected) {
                fail("Did not expected NeedsRefreshException!");
            }

            // Return the cache content from the exception
            content = nre.getCacheContent();
        }

        return content;
    }

    /**
     * Method that try to retrieve data from the cache but specify wrong arguments
     * <p>
     * @param key         The cache item key
     * @param refresh     The timestamp specifiying if the item needs refresh
     */
    private void invalidGetFromCacheArgument(String key, int refresh) {
        try {
            // Try to get the data from the cache
            admin.getFromCache(key, refresh);
            fail("getFromCache did NOT throw an IllegalArgumentException");
        } catch (IllegalArgumentException ipe) {
            // This is what we expect
        } catch (NeedsRefreshException nre) {
            admin.cancelUpdate(key);

            // Ignore this one
        }
    }

    /**
     * Method that try to insert data in the cache but specify wrong arguments
     * <p>
     * @param key         The cache item key
     * @param content     The content of the cache item
     */
    private void invalidPutInCacheArgument(String key, Object content) {
        try {
            // Try to put this data in the cache
            admin.putInCache(key, content);
            fail("putInCache did NOT throw an IllegalArgumentException");
        } catch (IllegalArgumentException ipe) {
            // This is what we expect
        }
    }

    private void populate() {
        for (int i = 0; i < 7; i++) {
            String[] groups = ((i & 1) == 0) ? new String[] {GROUP1, GROUP2} : new String[] {
                GROUP3
            };
            admin.putInCache(KEY + i, CONTENT + i, groups);
        }

        //register one miss.
        checkObj("Not in cache", NO_REFRESH_NEEDED, true);

        //register 7 hits
        for (int i = 0; i < 7; i++) {
            try {
                admin.getFromCache(KEY + i, NO_REFRESH_NEEDED);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(KEY + i);
            }
        }

        for (int i = 0; i < 7; i++) {
            try {
                admin.getFromCache(KEY + i, 0);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(KEY + i);
            }
        }

        admin.putInCache(KEY + 1, CONTENT);
        admin.putInCache(KEY + 2, CONTENT);
        admin.flushPattern("blahblah");
        admin.flushGroup(GROUP1);
        admin.flushGroup(GROUP2);
    }
}
