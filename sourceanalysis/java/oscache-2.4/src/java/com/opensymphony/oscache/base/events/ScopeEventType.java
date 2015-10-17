/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;


/**
 * This is an enumeration of all the possible events that may occur
 * at the scope level. Scope-level events are only relevant to the
 * <code>ServletCacheAdministrator</code>.
 *
 * @version        $Revision: 387 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class ScopeEventType {
    /**
     * Specifies an event type for the all scope flushed event.
     */
    public static final ScopeEventType ALL_SCOPES_FLUSHED = new ScopeEventType();

    /**
     * Specifies an event type for the flushing of a  specific scope.
     */
    public static final ScopeEventType SCOPE_FLUSHED = new ScopeEventType();

    /**
     * Private constructor to ensure that no object of that type are
     * created externally.
     */
    private ScopeEventType() {
    }
}
