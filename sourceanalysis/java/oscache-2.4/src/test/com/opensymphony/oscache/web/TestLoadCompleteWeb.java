/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.RandomTimer;

import junit.extensions.RepeatedTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.web package.
 * It invokes all the test suites of all the other classes of the package.
 * The test methods will be invoked with many users and iterations to simulate
 * load on request
 *
 * $Id: TestLoadCompleteWeb.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestLoadCompleteWeb extends TestCase {
    /**
     * Constructor for the osCache Cache project main test program
     */
    public TestLoadCompleteWeb(String str) {
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

        String[] args2 = {TestLoadCompleteWeb.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        final int clientThreads = 10; // Simulate 10 client threads
        final int iterations = 20; // Simulate each user doing 20 iterations

        TestSuite suite = new TestSuite("Test all osCache web");

        // Ramp up a thread each 500 ms (+-100ms) until total number of threads reached
        RandomTimer tm = new RandomTimer(300, 100);

        // JSP
        Test repeatedTest = new RepeatedTest(new TestOscacheJsp("testOscacheBasicForLoad"), iterations);
        Test loadTest = new LoadTest(repeatedTest, clientThreads, tm);
        suite.addTest(loadTest);

        // Servlet
        repeatedTest = new RepeatedTest(new TestOscacheServlet("testOscacheServletBasicForLoad"), iterations);
        loadTest = new LoadTest(repeatedTest, clientThreads, tm);
        suite.addTest(loadTest);

        // Filter
        repeatedTest = new RepeatedTest(new TestOscacheFilter("testOscacheFilterBasicForLoad"), iterations);
        loadTest = new LoadTest(repeatedTest, clientThreads, tm);
        suite.addTest(loadTest);

        return suite;
    }
}
