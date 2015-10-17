/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import java.util.Date;

/**
 * A <code>ScopeEvent</code> is created when an event occurs across one or all scopes.
 * This type of event is only applicable to the <code>ServletCacheAdministrator</code>.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class ScopeEvent extends CacheEvent {
    /**
     * Date that the event applies to.
     */
    private Date date = null;

    /**
     * Type of the event.
     */
    private ScopeEventType eventType = null;

    /**
     * Scope that applies to this event.
     */
    private int scope = 0;

    /**
     * Constructs a scope event object with no specified origin.
     *
     * @param eventType   Type of the event.
     * @param scope       Scope that applies to the event.
     * @param date        Date that the event applies to.
     */
    public ScopeEvent(ScopeEventType eventType, int scope, Date date) {
        this(eventType, scope, date, null);
    }

    /**
     * Constructs a scope event object.
     *
     * @param eventType   Type of the event.
     * @param scope       Scope that applies to the event.
     * @param date        Date that the event applies to.
     * @param origin      The origin of this event.
     */
    public ScopeEvent(ScopeEventType eventType, int scope, Date date, String origin) {
        super(origin);
        this.eventType = eventType;
        this.scope = scope;
        this.date = date;
    }

    /**
     * Retrieve the event date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Retrieve the type of the event.
     */
    public ScopeEventType getEventType() {
        return eventType;
    }

    /**
     * Retrieve the scope that applies to the event.
     */
    public int getScope() {
        return scope;
    }
}
