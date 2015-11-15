/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import java.util.Date;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheGroupEvent;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import com.opensymphony.oscache.base.events.ScopeEvent;
import com.opensymphony.oscache.base.events.ScopeEventType;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the cache entry event listener implementation
 *
 * $Id: TestCacheEntryEventListenerImpl.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 */
public class TestStatisticListenerImpl extends TestCase {
    
    private static final int PAGE_SCOPE = 1;

    /**
     * Key used for this test
     */
    private final String KEY = "Test Statistikc Listener Impl Key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestStatisticListenerImpl(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestStatisticListenerImpl.class);
    }

    /**
     * Test the basic implementation
     */
    public void testCacheEntryEventListenerImpl() {
        // Construct the objects required for the tests
        CacheEntry entry = new CacheEntry(KEY);
        GeneralCacheAdministrator admin = new GeneralCacheAdministrator();
        Cache cache = new Cache(admin.isMemoryCaching(), admin.isUnlimitedDiskCache(), admin.isOverflowPersistence());
        CacheEntryEvent event = new CacheEntryEvent(cache, entry, null);
        StatisticListenerImpl listener = new StatisticListenerImpl();

        // Assert the counters
        assertEquals(listener.getEntriesAdded(), 0);
        assertEquals(listener.getFlushCount(), 0);
        assertEquals(listener.getEntriesRemoved(), 0);
        assertEquals(listener.getEntriesUpdated(), 0);
        assertEquals(listener.getHitCount(), 0);
        assertEquals(listener.getHitCountSum(), 0);
        assertEquals(listener.getMissCount(), 0);
        assertEquals(listener.getMissCountSum(), 0);
        assertEquals(listener.getStaleHitCount(), 0);
        assertEquals(listener.getStaleHitCountSum(), 0);
        
        // Generate an event of each type
        listener.cacheEntryAdded(event);
        listener.cacheEntryFlushed(event);
        listener.cacheEntryRemoved(event);
        listener.cacheEntryUpdated(event);
        
        listener.scopeFlushed(new ScopeEvent(ScopeEventType.ALL_SCOPES_FLUSHED, PAGE_SCOPE, new Date()));
        listener.scopeFlushed(new ScopeEvent(ScopeEventType.SCOPE_FLUSHED, PAGE_SCOPE, new Date()));

        listener.cacheFlushed(new CachewideEvent(cache, new Date(), null));
        listener.cacheGroupFlushed(new CacheGroupEvent(cache, "testGroup"));
        listener.cachePatternFlushed(new CachePatternEvent(cache, "testPattern"));

        // Assert the counters
        assertEquals(listener.getEntriesAdded(), 1);
        assertEquals(listener.getFlushCount(), 6);
        assertEquals(listener.getEntriesRemoved(), 1);
        assertEquals(listener.getEntriesUpdated(), 1);
    }
}
