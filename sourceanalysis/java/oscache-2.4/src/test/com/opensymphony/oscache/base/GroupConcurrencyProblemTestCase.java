/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 254 $
 */
public class GroupConcurrencyProblemTestCase extends TestCase {
    private static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

    public static void main(String[] args) {
        System.out.println("START");

        // Create some clients and start them running.
        for (int i = 0; i < 100; i++) {
            System.out.println("Creating thread: " + i);

            new Client(i, cache).start();
        }

        System.out.println("END");
    }
}


/* Inner class to hammer away at the cache. */
class Client extends Thread {
    private static final int MAX_ITERATIONS = 1000;
    private GeneralCacheAdministrator cache;
    private int id;

    public Client(int newId, GeneralCacheAdministrator newCache) {
        super();
        id = newId;
        cache = newCache;
    }

    public void run() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            /* Put an entry from this Client into the shared group.
             */
            cache.putInCache(Integer.toString(id), "Some interesting data", new String[] {
                    "GLOBAL_GROUP"
                });

            // Flush that group.
            cache.flushGroup("GLOBAL_GROUP");
        }
    }
}
