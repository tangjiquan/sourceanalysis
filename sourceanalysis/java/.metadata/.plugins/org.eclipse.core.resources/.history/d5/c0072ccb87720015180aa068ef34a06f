/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is an enumeration of the cache events that represent the
 * various outcomes of cache accesses.
 *
 * @version $Revision: 387 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class CacheMapAccessEventType {
    /**
     * Get an event type for a cache hit.
     */
    public static final CacheMapAccessEventType HIT = new CacheMapAccessEventType();

    /**
     * Get an event type for a cache miss.
     */
    public static final CacheMapAccessEventType MISS = new CacheMapAccessEventType();

    /**
     * Get an event type for when the data was found in the cache but was stale.
     */
    public static final CacheMapAccessEventType STALE_HIT = new CacheMapAccessEventType();

    /**
     * Private constructor to ensure that no object of this type are
     * created externally.
     */
    private CacheMapAccessEventType() {
    }
}
