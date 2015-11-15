/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.base.events package.
 * It invokes all the test suites of all the other classes of the package.
 *
 * $Id: TestCompleteEvents.java 419 2007-03-17 13:01:19Z larst $
 * @version        $Revision: 419 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCompleteEvents extends TestCase {
    /**
     * Constructor for the oscache module main test program
     */
    public TestCompleteEvents(String str) {
        super(str);
    }

    /**
     * Main method which is called to perform the tests
     * <p>
     * @param   args    Arguments received
     */
    public static void main(String[] args) {
        // Run the test suite
        junit.swingui.TestRunner testRunner = new junit.swingui.TestRunner();
        testRunner.setLoading(false);

        String[] args2 = {TestCompleteEvents.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        // Add all the tests suite of all the project classes
        TestSuite suite = new TestSuite("Test all base cache modules");
        suite.addTest(TestCacheEntryEvent.suite());
        suite.addTest(TestCacheMapAccessEvent.suite());
        suite.addTest(TestScopeEvent.suite());
        suite.addTest(TestCachewideEvent.suite());
        suite.addTest(TestCachePatternEvent.suite());
        suite.addTest(TestCacheGroupEvent.suite());

        return suite;
    }
}
