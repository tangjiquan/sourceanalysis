/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.plugins.clustersupport package.
 * It invokes all the test suites of all the other classes of the package.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class TestCompleteClustering extends TestCase {
    /**
     * Constructor for the osCache project main test program
     */
    public TestCompleteClustering(String str) {
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

        String[] args2 = {TestCompleteClustering.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        // Add all the tests suite of all the project classes
        TestSuite suite = new TestSuite("Test all OSCache clustering");
        suite.addTest(TestJavaGroupsBroadcastingListener.suite());
        suite.addTest(TestJMSBroadcastingListener.suite());
        suite.addTest(TestJMS10BroadcastingListener.suite());

        return suite;
    }
}
