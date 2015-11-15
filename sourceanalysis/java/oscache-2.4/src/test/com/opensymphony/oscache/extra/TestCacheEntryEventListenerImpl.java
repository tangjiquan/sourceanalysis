/*
 * Copyright (c) 2002-2003 by OpenSymphony
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
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the cache entry event listener implementation
 *
 * $Id: TestCacheEntryEventListenerImpl.java 418 2007-03-17 12:18:51Z larst $
 * @version        $Revision: 418 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestCacheEntryEventListenerImpl extends TestCase {
    /**
     * Key used for this test
     */
    private final String KEY = "Test Cache Entry Event Listener Impl Key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestCacheEntryEventListenerImpl(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestCacheEntryEventListenerImpl.class);
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
        CacheEntryEventListenerImpl listener = new CacheEntryEventListenerImpl();

        // Assert the counters
        assertEquals(listener.getEntryAddedCount(), 0);
        assertEquals(listener.getEntryFlushedCount(), 0);
        assertEquals(listener.getEntryRemovedCount(), 0);
        assertEquals(listener.getEntryUpdatedCount(), 0);
        assertEquals(listener.getGroupFlushedCount(), 0);
        assertEquals(listener.getPatternFlushedCount(), 0);
        assertEquals(listener.getCacheFlushedCount(), 0);

        // Generate an event of each type
        listener.cacheEntryAdded(event);
        listener.cacheEntryFlushed(event);
        listener.cacheEntryRemoved(event);
        listener.cacheEntryUpdated(event);

        listener.cacheFlushed(new CachewideEvent(cache, new Date(), null));
        listener.cacheGroupFlushed(new CacheGroupEvent(cache, "testGroup", null));
        listener.cachePatternFlushed(new CachePatternEvent(cache, "testPattern", null));

        // Assert the counters
        assertEquals(listener.getEntryAddedCount(), 1);
        assertEquals(listener.getEntryFlushedCount(), 1);
        assertEquals(listener.getEntryRemovedCount(), 1);
        assertEquals(listener.getEntryUpdatedCount(), 1);
        assertEquals(listener.getGroupFlushedCount(), 1);
        assertEquals(listener.getPatternFlushedCount(), 1);
    }
}
