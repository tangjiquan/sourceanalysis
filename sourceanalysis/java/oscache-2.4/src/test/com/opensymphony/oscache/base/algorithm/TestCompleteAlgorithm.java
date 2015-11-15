/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.base.algorithm package.
 * It invokes all the test suites of all the other classes of the package,
 * except abstract ones because they are tested via final ones.
 *
 * $Id: TestCompleteAlgorithm.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestCompleteAlgorithm extends TestCase {
    /**
     * Constructor for the oscache project main test program
     */
    public TestCompleteAlgorithm(String str) {
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

        String[] args2 = {TestCompleteAlgorithm.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        // Add all the tests suite of all the project classes
        TestSuite suite = new TestSuite("Test all base algorithm cache modules");
        suite.addTest(TestFIFOCache.suite());
        suite.addTest(TestLRUCache.suite());
        suite.addTest(TestUnlimitedCache.suite());

        return suite;
    }
}
