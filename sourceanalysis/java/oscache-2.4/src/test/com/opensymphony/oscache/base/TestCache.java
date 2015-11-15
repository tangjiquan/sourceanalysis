/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import java.util.Properties;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the public methods of the Cache class
 *
 * $Id: TestCache.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestCache extends TestCase {
    // Static variables required thru all the tests
    private static Cache map = null;
    private final String CONTENT = "Content for the cache test";

    // Constants needed thru all the tests
    private final String ENTRY_KEY = "Test cache key";
    private final int NO_REFRESH_NEEDED = CacheEntry.INDEFINITE_EXPIRY;
    private final int REFRESH_NEEDED = 0;

    /**
     * Class constructor.
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCache(String str) {
        super(str);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a new Cache
        if (map == null) {
            GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
            map = admin.getCache();
            assertNotNull(map);
        }
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestCache.class);
    }

    /**
     * Verify that items may still be flushed by key pattern
     */
    public void testFlushPattern() {
        // Try to flush with a bad pattern and ensure that our data is still there
        map.putInCache(ENTRY_KEY, CONTENT);
        map.flushPattern(ENTRY_KEY + "do not flush");
        getBackContent(map, CONTENT, NO_REFRESH_NEEDED, false);

        // Flush our map for real
        map.flushPattern(ENTRY_KEY.substring(1, 2));
        getBackContent(map, CONTENT, NO_REFRESH_NEEDED, true);

        // Check invalid values
        map.flushPattern("");
        map.flushPattern(null);
    }

    /**
     * Tests that with a very large amount of keys that added and trigger cache overflows, there is no memory leak
     * @throws Exception
     */
    public void testBug174CacheOverflow() throws Exception {
        
        Properties p = new Properties();
		p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.LRUCache");
		p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, "100");
		GeneralCacheAdministrator admin = new GeneralCacheAdministrator(p);
        
        int cacheCapacity = 100;
		int maxAddedCacheEntries = cacheCapacity*10;
        String baseCacheKey= "baseKey";
        String cacheValue ="same_value";

		admin.setCacheCapacity(cacheCapacity);
    	
        Cache cache = admin.getCache();

        //Add lots of different keys to trigger cache overflow
		for (int keyIndex=0; keyIndex<maxAddedCacheEntries; keyIndex++) {
			String key = baseCacheKey + keyIndex;
			admin.putInCache(key, cacheValue);
		}
        
		Assert.assertEquals("expected cache to be at its full capacity", cacheCapacity , cache.getSize());
		Assert.assertTrue("expected cache overflows to have cleaned UpdateState instances. got [" + cache.getNbUpdateState() + "] updates while max is [" + cacheCapacity + "]", cache.getNbUpdateState() <= cacheCapacity);
    }

    /**
     * Tests that with a very large amount of keys that added and trigger cache overflows, there is no memory leak
     * @throws Exception
     */
    public void testBug174CacheOverflowAndUpdate() throws Exception {
    	Properties p = new Properties();
		p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.LRUCache");
		p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, "100");
		GeneralCacheAdministrator admin = new GeneralCacheAdministrator(p);
        
        int cacheCapacity = 100;
		int maxAddedCacheEntries = cacheCapacity*10;
        String baseCacheKey= "baseKey";
        String cacheValue ="same_value";

		admin.setCacheCapacity(cacheCapacity);
    	
        Cache cache = admin.getCache();

        
        //Add lots of different keys to trigger cache overflow, mixed with updates
		//FIXME: we may need different threads to enter branches recovering from current update.  
		for (int keyIndex=0; keyIndex<maxAddedCacheEntries; keyIndex++) {
			String key = baseCacheKey + keyIndex;
			admin.putInCache(key, cacheValue);
			
			try {
				admin.getFromCache(key, 0);
				fail("expected element [" + key + "] not to be present");
			} catch (NeedsRefreshException e) {
				admin.putInCache(key, cacheValue);
			}
		}
        
		Assert.assertEquals("expected cache to be at its full capacity", cacheCapacity , cache.getSize());
		Assert.assertTrue("expected cache overflows to have cleaned UpdateState instances. Nb states is:" + cache.getNbUpdateState() + " expected max="+ cacheCapacity, cache.getNbUpdateState() <= cacheCapacity);
    }

    
    /**
     * Tests that with a very large amount of keys accessed and cancelled, there is no memory leak
     * @throws Exception
     */
    public void testBug174CacheMissesNonBlocking() throws Exception {
    	testBug174CacheMisses(false);
    }
    
    /**
     * Tests that with a very large amount of keys accessed and cancelled, there is no memory leak
     * @throws Exception
     */
    public void testBug174CacheMissesBlocking() throws Exception {
    	testBug174CacheMisses(true);
    }

    /**
     * Tests that with a very large amount of keys accessed and cancelled, there is no memory leak
     * @throws Exception
     */
    public void testBug174CacheMisses(boolean block) throws Exception {
    	Properties p = new Properties();
		p.setProperty(AbstractCacheAdministrator.CACHE_ALGORITHM_KEY, "com.opensymphony.oscache.base.algorithm.LRUCache");
		p.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, "100");
		if (block) {
			p.setProperty(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, "true");
		}
		GeneralCacheAdministrator admin = new GeneralCacheAdministrator(p);
        
        int cacheCapacity = 100;
		int maxAddedCacheEntries = cacheCapacity*10;
        String baseCacheKey= "baseKey";
        //String cacheValue ="same_value";
        
		admin.setCacheCapacity(cacheCapacity);
    	
        Cache cache = admin.getCache();

        //Access lots of different keys to trigger cache overflow
		for (int keyIndex=0; keyIndex<maxAddedCacheEntries; keyIndex++) {
			String key = baseCacheKey + keyIndex;
			try {
				admin.getFromCache(key);
				fail("expected element [" + key + "] not to be present");
			} catch (NeedsRefreshException e) {
				admin.cancelUpdate(key);
			}
		}
        
		Assert.assertTrue("expected cache accesses to not leak past cache capacity. Nb states is:" + cache.getNbUpdateState() + " expected max="+ cacheCapacity, cache.getNbUpdateState() < cacheCapacity);
    }
    
    /**
     * Verify that we can put item in the cache and that they are correctly retrieved
     */
    public void testPutGetFromCache() {
        // We put content in the cache and get it back with and without refresh
        map.putInCache(ENTRY_KEY, CONTENT);
        getBackContent(map, CONTENT, NO_REFRESH_NEEDED, false);
        getBackContent(map, CONTENT, REFRESH_NEEDED, true);

        // Test with invalid values

        /** TODO Verify this logic */
        try {
            assertNull(map.getFromCache("", NO_REFRESH_NEEDED));
        } catch (NeedsRefreshException nre) {
            map.cancelUpdate("");
        } catch (Exception e) {
        }

        try {
            assertNull(map.getFromCache(null, NO_REFRESH_NEEDED));
        } catch (NeedsRefreshException nre) {
            map.cancelUpdate(null);
        } catch (Exception e) {
        }
    }

    /**
     * Verify that we can put item in the cache and that they are correctly retrieved
     */
    public void testPutGetFromCacheWithPolicy() {
        // We put content in the cache and get it back
        map.putInCache(ENTRY_KEY + "policy", CONTENT, new DummyAlwayRefreshEntryPolicy());

        // Should get a refresh
        try {
            map.getFromCache(ENTRY_KEY + "policy", -1);
            fail("Should have got a refresh.");
        } catch (NeedsRefreshException nre) {
            map.cancelUpdate(ENTRY_KEY + "policy");
        }
    }

    protected void tearDown() throws Exception {
        if (map != null) {
            map.clear();
        }
    }

    /**
     * Retrieve the content in the cache
     * <p>
     * @param map       The Cache in which the data is stored
     * @param content   The content expected to be retrieved
     * @param refresh   Time interval to determine if the cache object needs refresh
     * @param exceptionExpected Specify if a NeedsRefreshException is expected
     */
    private void getBackContent(Cache map, Object content, int refresh, boolean exceptionExpected) {
        try {
            assertEquals(content, map.getFromCache(ENTRY_KEY, refresh));

            if (exceptionExpected) {
                fail("NeedsRefreshException should have been thrown!");
            }
        } catch (NeedsRefreshException nre) {
            map.cancelUpdate(ENTRY_KEY);

            if (!exceptionExpected) {
                fail("NeedsRefreshException shouldn't have been thrown!");
            }
        }
    }
}
