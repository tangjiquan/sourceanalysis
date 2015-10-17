/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;

/**
 * CacheGroupEvent is an event that occurs at the cache group level
 * (Add, update, remove, flush). It contains the group name and the
 * originating cache object.
 *
 * @version        $Revision: 254 $
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CacheGroupEvent extends CacheEvent {
    /**
     * The cache where the entry resides.
     */
    private Cache map = null;

    /**
     * The group that the event applies to.
     */
    private String group = null;

    /**
     * Constructs a cache group event with no origin
     *
     * @param map     The cache map of the cache entry
     * @param group   The cache group that the event applies to.
     */
    public CacheGroupEvent(Cache map, String group) {
        this(map, group, null);
    }

    /**
     * Constructs a cache group event
     *
     * @param map     The cache map of the cache entry
     * @param group   The cache group that the event applies to.
     * @param origin  An optional tag that can be attached to the event to
     * specify the event's origin. This is useful to prevent events from being
     * fired recursively in some situations, such as when an event handler
     * causes another event to be fired.
     */
    public CacheGroupEvent(Cache map, String group, String origin) {
        super(origin);
        this.map = map;
        this.group = group;
    }

    /**
     * Retrieve the cache group that the event applies to.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Retrieve the cache map where the group resides.
     */
    public Cache getMap() {
        return map;
    }

    public String toString() {
        return "groupName=" + group;
    }
}
