/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;


/**
 * This is an dummy implementation of an EntryRefreshPolicy. It is just to
 * illustrate how to use it.
 *
 * $Id: DummyAlwayRefreshEntryPolicy.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class DummyAlwayRefreshEntryPolicy implements EntryRefreshPolicy {
    /**
     * Dummy implementation of an entry refresh policy. A real implementation
     * whould do some logic to determine if this entry needs to be refreshed.
     * It can be calling a bean or checking some files, or even manually manage
     * the time expiration.
     *
     * <p>
     * @param entry  The entry for wich to determine if a refresh is needed
     * @return True or false
     */
    public boolean needsRefresh(CacheEntry entry) {
        return true;
    }
}
