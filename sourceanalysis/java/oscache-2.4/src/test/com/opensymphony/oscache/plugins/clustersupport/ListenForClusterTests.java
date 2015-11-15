/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.FinalizationException;
import com.opensymphony.oscache.base.InitializationException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>This should be used in conjunction with the cluster test cases. Run this
 * program to set up listeners for the various clustering implementations so
 * you can see that the test messages are being received correctly.</p>
 *
 * <p>A shutdown hook is installed so the listeners can be shut down cleanly.</p>
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class ListenForClusterTests {
    ArrayList listeners = new ArrayList();
    Cache cache;

    private void mainLoop() {
        Thread shutdownHook = new ShutdownHookThread("");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("Waiting for cluster messages... (CTRL-C to exit)");
        System.out.println("------------------------------------------------");

        while (true) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ie) {
            }
        }
    }

    private void initListeners() {
        BaseTestBroadcastingListener testcase = null;
        AbstractBroadcastingListener listener;
        Cache cache = new Cache(true, false, false);

        // Add the JavaGroups listener
        try {
            testcase = new TestJavaGroupsBroadcastingListener("JavaGroups");
            listener = testcase.getListener();
            listener.initialize(cache, testcase.getConfig());
            cache.addCacheEventListener(listener);
            listeners.add(listener);
        } catch (InitializationException e) {
            System.out.println("The JavaGroups listener could not be initialized: " + e);
        }

        // Add the JMS listener
        try {
            testcase = new TestJMSBroadcastingListener("JMS");
            listener = testcase.getListener();

            Config config = testcase.getConfig();
            config.set("cache.cluster.jms.node.name", "cacheNode2");

            listener.initialize(cache, config);
            cache.addCacheEventListener(listener);
            listeners.add(listener);
        } catch (InitializationException e) {
            System.out.println("The JMS listener could not be initialized: " + e);
        }
    }

    /**
     * Starts up the cluster listeners.
     */
    public static void main(String[] args) {
        ListenForClusterTests listen = new ListenForClusterTests();

        listen.initListeners();

        listen.mainLoop();
    }

    /**
     * Inner class that handles the shutdown event
     */
    class ShutdownHookThread extends Thread {
        protected String message;

        public ShutdownHookThread(String message) {
            this.message = message;
        }

        /**
         * This is executed when the application is forcibly shutdown (via
         * CTRL-C etc). Any configured listeners are shut down here.
         */
        public void run() {
            System.out.println("Shutting down the cluster listeners...");

            for (Iterator it = listeners.iterator(); it.hasNext();) {
                try {
                    ((AbstractBroadcastingListener) it.next()).finialize();
                } catch (FinalizationException e) {
                    System.out.println("The listener could not be shut down cleanly: " + e);
                }
            }

            System.out.println("Shutdown complete.");
        }
    }
}
