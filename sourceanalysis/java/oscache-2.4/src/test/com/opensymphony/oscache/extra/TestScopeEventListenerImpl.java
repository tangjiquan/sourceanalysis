/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import com.opensymphony.oscache.base.events.ScopeEvent;
import com.opensymphony.oscache.base.events.ScopeEventType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;

/**
 * Test the scope event listener implementation
 *
 * $Id: TestScopeEventListenerImpl.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestScopeEventListenerImpl extends TestCase {
    private static final int PAGE_SCOPE = 1;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestScopeEventListenerImpl(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestScopeEventListenerImpl.class);
    }

    /**
     * Test the basic implementation of this listener
     */
    public void testScopeEventListenerImpl() {
        // Construct the object we need for the test
        ScopeEventListenerImpl listener = new ScopeEventListenerImpl();

        // Generates events
        listener.scopeFlushed(new ScopeEvent(ScopeEventType.ALL_SCOPES_FLUSHED, PAGE_SCOPE, new Date()));
        listener.scopeFlushed(new ScopeEvent(ScopeEventType.SCOPE_FLUSHED, PAGE_SCOPE, new Date()));

        // Assert the counters
        assertEquals(listener.getApplicationScopeFlushCount(), 1);
        assertEquals(listener.getPageScopeFlushCount(), 2);
        assertEquals(listener.getRequestScopeFlushCount(), 1);
        assertEquals(listener.getSessionScopeFlushCount(), 1);
        assertEquals(listener.getTotalScopeFlushCount(), 5);
    }
}
