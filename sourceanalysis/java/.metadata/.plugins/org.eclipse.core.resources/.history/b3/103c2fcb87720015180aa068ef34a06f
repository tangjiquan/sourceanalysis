/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;

/**
 * A CachePatternEvent is fired when a pattern has been applied to a cache.
 *
 * @version        $Revision: 254 $
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CachePatternEvent extends CacheEvent {
    /**
     * The cache the pattern is being applied to.
     */
    private Cache map = null;

    /**
     * The pattern that is being applied.
     */
    private String pattern = null;

    /**
     * Constructs a cache pattern event with no origin
     *
     * @param map     The cache map that the pattern was applied to
     * @param pattern The pattern that was applied
     */
    public CachePatternEvent(Cache map, String pattern) {
        this(map, pattern, null);
    }

    /**
     * Constructs a cache pattern event
     *
     * @param map     The cache map that the pattern was applied to
     * @param pattern The cache pattern that the event applies to.
     * @param origin  An optional tag that can be attached to the event to
     * specify the event's origin. This is useful to prevent events from being
     * fired recursively in some situations, such as when an event handler
     * causes another event to be fired, or for logging purposes.
     */
    public CachePatternEvent(Cache map, String pattern, String origin) {
        super(origin);
        this.map = map;
        this.pattern = pattern;
    }

    /**
     * Retrieve the cache map that had the pattern applied.
     */
    public Cache getMap() {
        return map;
    }

    /**
     * Retrieve the pattern that was applied to the cache.
     */
    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return "pattern=" + pattern;
    }
}
