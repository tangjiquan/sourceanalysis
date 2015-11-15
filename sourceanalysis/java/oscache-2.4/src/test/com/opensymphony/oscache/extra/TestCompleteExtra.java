/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.extra;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.extra package.
 * It invokes all the test suites of all the other classes of the package.
 *
 * $Id: TestCompleteExtra.java 417 2007-03-17 11:42:20Z larst $
 * @version        $Revision: 417 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCompleteExtra extends TestCase {
    /**
     * Constructor for the osCache Cache Extra package main test program
     */
    public TestCompleteExtra(String str) {
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

        String[] args2 = {TestCompleteExtra.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        // Add all the test suites of all the project classes
        TestSuite suite = new TestSuite("Test all extra cache modules");
        suite.addTest(TestCacheEntryEventListenerImpl.suite());
        suite.addTest(TestCacheMapAccessEventListenerImpl.suite());
        suite.addTest(TestScopeEventListenerImpl.suite());
        suite.addTest(TestStatisticListenerImpl.suite());

        return suite;
    }
}
