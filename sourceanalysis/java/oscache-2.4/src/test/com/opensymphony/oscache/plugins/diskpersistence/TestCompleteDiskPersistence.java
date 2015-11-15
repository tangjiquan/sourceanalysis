/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.diskpersistence;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for the com.opensymphony.oscache.plugins.diskpersistence package.
 * It invokes all the test suites of all the other classes of the package.
 *
 * $Id: TestCompleteDiskPersistence.java 254 2005-06-17 05:07:38Z dres $
 * @version        $Revision: 254 $
 * @author Lars Torunski
 */
public final class TestCompleteDiskPersistence extends TestCase {
    /**
     * Constructor for the osCache Cache Extra package main test program
     */
    public TestCompleteDiskPersistence(String str) {
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

        String[] args2 = {TestCompleteDiskPersistence.class.getName()};
        testRunner.start(args2);
    }

    /**
     * Test suite required to test this project
     * <p>
     * @return  suite   The test suite
     */
    public static Test suite() {
        // Add all the test suites of all the project classes
        TestSuite suite = new TestSuite("Test all diskpersistence plugins");
        suite.addTest(TestDiskPersistenceListener.suite());
        suite.addTest(TestHashDiskPersistenceListener.suite());
        //suite.addTest(TestUnSerializable.suite());

        return suite;
    }
}
