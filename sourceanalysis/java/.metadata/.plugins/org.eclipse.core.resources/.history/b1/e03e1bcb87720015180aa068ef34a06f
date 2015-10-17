/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is the interface to listen to cache entry events. There is a method
 * for each event type. These methods are called via a dispatcher. If you
 * want to be notified when an event occurs on an entry, group or across a
 * pattern, register a listener and implement this interface.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public interface CacheEntryEventListener extends CacheEventListener {
    /**
     * Event fired when an entry is added to the cache.
     */
    void cacheEntryAdded(CacheEntryEvent event);

    /**
     * Event fired when an entry is flushed from the cache.
     */
    void cacheEntryFlushed(CacheEntryEvent event);

    /**
     * Event fired when an entry is removed from the cache.
     */
    void cacheEntryRemoved(CacheEntryEvent event);

    /**
     * Event fired when an entry is updated in the cache.
     */
    void cacheEntryUpdated(CacheEntryEvent event);

    /**
     * Event fired when a group is flushed from the cache.
     */
    void cacheGroupFlushed(CacheGroupEvent event);

    /**
     * Event fired when a key pattern is flushed from the cache.
     * Note that this event will <em>not</em> be fired if the pattern
     * is <code>null</code> or an empty string, instead the flush
     * request will silently be ignored.
     */
    void cachePatternFlushed(CachePatternEvent event);

    /**
     * An event that is fired when an entire cache gets flushed.
     */
    void cacheFlushed(CachewideEvent event);
}
