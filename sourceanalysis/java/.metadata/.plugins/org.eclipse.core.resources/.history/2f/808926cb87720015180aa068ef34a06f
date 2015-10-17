/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.CacheEntry;

/**
 * Cache map access event. This is the object created when an event occurs on a
 * cache map (cache Hit, cache miss). It contains the entry that was referenced
 * by the event and the event type.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class CacheMapAccessEvent extends CacheEvent {
    /**
     * The cache entry that the event applies to.
     */
    private CacheEntry entry = null;

    /**
     * Type of the event.
     */
    private CacheMapAccessEventType eventType = null;

    /**
     * Constructor.
     * <p>
     * @param eventType   Type of the event.
     * @param entry       The cache entry that the event applies to.
     */
    public CacheMapAccessEvent(CacheMapAccessEventType eventType, CacheEntry entry) {
        this(eventType, entry, null);
    }

    /**
     * Constructor.
     * <p>
     * @param eventType   Type of the event.
     * @param entry       The cache entry that the event applies to.
     * @param origin      The origin of the event
     */
    public CacheMapAccessEvent(CacheMapAccessEventType eventType, CacheEntry entry, String origin) {
        super(origin);
        this.eventType = eventType;
        this.entry = entry;
    }

    /**
     * Retrieve the cache entry that the event applies to.
     */
    public CacheEntry getCacheEntry() {
        return entry;
    }

    /**
     * Retrieve the cache entry key that the event applies to.
     */
    public String getCacheEntryKey() {
        return entry.getKey();
    }

    /**
     * Retrieve the type of the event.
     */
    public CacheMapAccessEventType getEventType() {
        return eventType;
    }
}
