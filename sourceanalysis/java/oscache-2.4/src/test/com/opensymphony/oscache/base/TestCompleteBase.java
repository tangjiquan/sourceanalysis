/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.base.algorithm.TestCompleteAlgorithm;
import com.opensymphony.oscache.base.events.TestCompleteEvents;
import com.opensymphony.oscache.util.TestFastCronParser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.base package.
 * It invokes all the test suites of all the other classes of the package.
 *
 * $Id: TestCompleteBase.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCompleteBase extends TestCase {
    /**
     * Constructor for the osCache project main test program
     */
    public TestCompleteBase(String str) {
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

        String[] args2 = {TestCompleteBase.class.getName()};
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
        suite.addTest(TestFastCronParser.suite());
        suite.addTest(TestCacheEntry.suite());
        suite.addTest(TestCache.suite());
        suite.addTest(TestConcurrency.suite());
        suite.addTest(TestCompleteAlgorithm.suite());
        suite.addTest(TestCompleteEvents.suite());

        return suite;
    }
}
