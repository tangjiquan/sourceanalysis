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
public final class TestJMSBroadcastingListener extends BaseTestBroadcastingListener {
    public TestJMSBroadcastingListener(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit.
     *
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestJMSBroadcastingListener.class);
    }

    /**
     * Returns a configured JavaGroupsBroadcastingListener instance
     * for testing.
     */
    public AbstractBroadcastingListener getListener() {
        return new JMSBroadcastingListener();
    }

    /**
     * Return the configuration for the JMS listener
     */
    Config getConfig() {
        Config config = new Config();

        // There needs to be an application resource file present "jndi.properties" that contains the following
        // parameters:
        //        config.set(Context.INITIAL_CONTEXT_FACTORY, "com.evermind.server.ApplicationClientInitialContextFactory");
        //        config.set(Context.PROVIDER_URL, "ormi://localhost:23791/");
        //        config.set(Context.SECURITY_PRINCIPAL, "admin");
        //        config.set(Context.SECURITY_CREDENTIALS, "xxxxxx");
        config.set("cache.cluster.jms.topic.factory", "java:comp/env/jms/TopicConnectionFactory");
        config.set("cache.cluster.jms.topic.name", "java:comp/env/jms/OSCacheTopic");
        config.set("cache.cluster.jms.node.name", "cacheNode1");

        return config;
    }
}
