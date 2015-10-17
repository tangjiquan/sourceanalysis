/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;

/**
 * CacheEntryEvent is the object created when an event occurs on a
 * cache entry (Add, update, remove, flush). It contains the entry itself and
 * its map.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class CacheEntryEvent extends CacheEvent {
    /**
     * The cache where the entry resides.
     */
    private Cache map = null;

    /**
     * The entry that the event applies to.
     */
    private CacheEntry entry = null;

    /**
     * Constructs a cache entry event object with no specified origin
     *
     * @param map     The cache map of the cache entry
     * @param entry   The cache entry that the event applies to
     */
    public CacheEntryEvent(Cache map, CacheEntry entry) {
        this(map, entry, null);
    }

    /**
     * Constructs a cache entry event object
     *
     * @param map     The cache map of the cache entry
     * @param entry   The cache entry that the event applies to
     * @param origin  The origin of this event
     */
    public CacheEntryEvent(Cache map, CacheEntry entry, String origin) {
        super(origin);
        this.map = map;
        this.entry = entry;
    }

    /**
     * Retrieve the cache entry that the event applies to.
     */
    public CacheEntry getEntry() {
        return entry;
    }

    /**
     * Retrieve the cache entry key
     */
    public String getKey() {
        return entry.getKey();
    }

    /**
     * Retrieve the cache map where the entry resides.
     */
    public Cache getMap() {
        return map;
    }

    public String toString() {
        return "key=" + entry.getKey();
    }
}
