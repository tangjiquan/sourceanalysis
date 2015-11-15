/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.Config;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all the public methods of the broadcasting listener and assert the
 * return values
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class TestJavaGroupsBroadcastingListener extends BaseTestBroadcastingListener {
    public TestJavaGroupsBroadcastingListener(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit.
     *
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestJavaGroupsBroadcastingListener.class);
    }

    /**
     * Returns a configured JavaGroupsBroadcastingListener instance
     * for testing.
     */
    public AbstractBroadcastingListener getListener() {
        return new JavaGroupsBroadcastingListener();
    }

    /**
     * Get the configuration for this listener
     */
    public Config getConfig() {
        Config config = new Config();

        // Just specify the IP and leave the rest of the settings at
        // default values.
        config.set("cache.cluster.multicast.ip", "231.12.21.132");

        return config;
    }
}
