/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;

import java.util.Date;

/**
 * A <code>CachewideEvent<code> represents and event that occurs on
 * the the entire cache, eg a cache flush or clear.
 *
 * @version $Revision: 254 $
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CachewideEvent extends CacheEvent {
    /**
     * The cache where the event occurred.
     */
    private Cache cache = null;

    /**
     * The date/time for when the flush is scheduled
     */
    private Date date = null;

    /**
     * Constructs a cachewide event with the specified origin.
     *
     * @param cache   The cache map that the event occurred on.
     * @param date    The date/time that this cachewide event is scheduled for
     * (eg, the date that the cache is to be flushed).
     * @param origin  An optional tag that can be attached to the event to
     * specify the event's origin. This is useful to prevent events from being
     * fired recursively in some situations, such as when an event handler
     * causes another event to be fired.
     */
    public CachewideEvent(Cache cache, Date date, String origin) {
        super(origin);
        this.date = date;
        this.cache = cache;
    }

    /**
     * Retrieve the cache map that the event occurred on.
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Retrieve the date/time that the cache flush is scheduled for.
     */
    public Date getDate() {
        return date;
    }
}
