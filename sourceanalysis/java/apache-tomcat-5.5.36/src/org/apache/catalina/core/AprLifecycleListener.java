/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.StringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jni.Library;



/**
 * Implementation of <code>LifecycleListener</code> that will init and
 * and destroy APR.
 * 
 * @author Remy Maucherat
 * @author Filip Hanik
 * @version $Id: AprLifecycleListener.java 1171683 2011-09-16 17:21:25Z jim $
 * @since 4.1
 */

public class AprLifecycleListener
    implements LifecycleListener {

    private static Log log = LogFactory.getLog(AprLifecycleListener.class);

    private static boolean instanceCreated = false;
    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ---------------------------------------------- Constants


    protected static final int REQUIRED_MAJOR = 1;
    protected static final int REQUIRED_MINOR = 1;
    protected static final int REQUIRED_PATCH = 17;
    protected static final int RECOMMENDED_PV = 22;


    // ---------------------------------------------- Properties
    protected static String SSLEngine = "on"; //default on
    protected static String SSLRandomSeed = "builtin";
    protected static boolean sslInitialized = false;
    protected static boolean aprInitialized = false;
    protected static boolean sslAvailable = false;
    protected static boolean aprAvailable = false;

    public static boolean isAprAvailable() {
        //https://issues.apache.org/bugzilla/show_bug.cgi?id=48613
        if (instanceCreated) init();
        return aprAvailable;
    }
    
    public AprLifecycleListener() {
        instanceCreated = true;
    }

    // ---------------------------------------------- LifecycleListener Methods

    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (Lifecycle.INIT_EVENT.equals(event.getType())) {
            init();
            if (aprAvailable) {
                try {
                    initializeSSL();
                } catch (Throwable t) {
                    if (!log.isDebugEnabled()) {
                        log.info(sm.getString("aprListener.sslInit"));
                    } else {
                        log.debug(sm.getString("aprListener.sslInit"), t);
                    }
                }
            }
        } else if (Lifecycle.AFTER_STOP_EVENT.equals(event.getType())) {
            if (!aprAvailable) {
                return;
            }
            try {
                terminateAPR();
            } catch (Throwable t) {
                if (!log.isDebugEnabled()) {
                    log.info(sm.getString("aprListener.aprDestroy"));
                } else {
                    log.debug(sm.getString("aprListener.aprDestroy"), t);
                }
            }
        }

    }

    private static synchronized void terminateAPR()
        throws ClassNotFoundException, NoSuchMethodException,
               IllegalAccessException, InvocationTargetException
    {
        String methodName = "terminate";
        Method method = Class.forName("org.apache.tomcat.jni.Library")
            .getMethod(methodName, (Class [])null);
        method.invoke(null, (Object []) null);
    }

    private static void init()
    {
        int major = 0;
        int minor = 0;
        int patch = 0;
        if (aprInitialized) {
            return;    
        }
        aprInitialized = true;
        
        try {
            String methodName = "initialize";
            Class paramTypes[] = new Class[1];
            paramTypes[0] = String.class;
            Object paramValues[] = new Object[1];
            paramValues[0] = null;
            Class clazz = Class.forName("org.apache.tomcat.jni.Library");
            Method method = clazz.getMethod(methodName, paramTypes);
            method.invoke(null, paramValues);
            major = clazz.getField("TCN_MAJOR_VERSION").getInt(null);
            minor = clazz.getField("TCN_MINOR_VERSION").getInt(null);
            patch = clazz.getField("TCN_PATCH_VERSION").getInt(null);
        } catch (Throwable t) {
            if (!log.isDebugEnabled()) {
                log.info(sm.getString("aprListener.aprInit",
                        System.getProperty("java.library.path")));
            } else {
                log.debug(sm.getString("aprListener.aprInit",
                        System.getProperty("java.library.path")), t);
            }
            return;
        }
        if ((major != REQUIRED_MAJOR)  ||
            (minor != REQUIRED_MINOR) ||
            (patch <  REQUIRED_PATCH)) {
            log.error(sm.getString("aprListener.tcnInvalid", major + "."
                    + minor + "." + patch,
                    REQUIRED_MAJOR + "." +
                    REQUIRED_MINOR + "." +
                    REQUIRED_PATCH));
            try {
                // Terminate the APR in case the version
                // is below required.                
                terminateAPR();
            } catch (Throwable t) {
                // Ignore
            }
            return;
        }
        if (patch <  RECOMMENDED_PV) {
            if (!log.isDebugEnabled()) {
                log.info(sm.getString("aprListener.tcnVersion", major + "."
                        + minor + "." + patch,
                        REQUIRED_MAJOR + "." +
                        REQUIRED_MINOR + "." +
                        RECOMMENDED_PV));
            } else {
                log.debug(sm.getString("aprListener.tcnVersion", major + "."
                        + minor + "." + patch,
                        REQUIRED_MAJOR + "." +
                        REQUIRED_MINOR + "." +
                        RECOMMENDED_PV));
            }
        }
        if (!log.isDebugEnabled()) {
           log.info(sm.getString("aprListener.tcnValid", major + "."
                    + minor + "." + patch));
        }
        else {
           log.debug(sm.getString("aprListener.tcnValid", major + "."
                     + minor + "." + patch));
        }
        // Log APR flags
        log.info(sm.getString("aprListener.flags",
                Boolean.valueOf(Library.APR_HAVE_IPV6),
                Boolean.valueOf(Library.APR_HAS_SENDFILE),
                Boolean.valueOf(Library.APR_HAS_SO_ACCEPTFILTER),
                Boolean.valueOf(Library.APR_HAS_RANDOM)));
        aprAvailable = true;
    }

    private static synchronized void initializeSSL()
        throws ClassNotFoundException, NoSuchMethodException,
               IllegalAccessException, InvocationTargetException
    {

        if ("off".equalsIgnoreCase(SSLEngine)) {
            return;
        }
        if (sslInitialized) {
             //only once per VM
            return;
        }
        sslInitialized = true;

        String methodName = "randSet";
        Class paramTypes[] = new Class[1];
        paramTypes[0] = String.class;
        Object paramValues[] = new Object[1];
        paramValues[0] = SSLRandomSeed;
        Class clazz = Class.forName("org.apache.tomcat.jni.SSL");
        Method method = clazz.getMethod(methodName, paramTypes);
        method.invoke(null, paramValues);
        

        methodName = "initialize";
        paramValues[0] = "on".equalsIgnoreCase(SSLEngine)?null:SSLEngine;
        method = clazz.getMethod(methodName, paramTypes);
        method.invoke(null, paramValues);
 
        sslAvailable = true;
    }

    public String getSSLEngine() {
        return SSLEngine;
    }

    public void setSSLEngine(String SSLEngine) {
        this.SSLEngine = SSLEngine;
    }

    public String getSSLRandomSeed() {
        return SSLRandomSeed;
    }

    public void setSSLRandomSeed(String SSLRandomSeed) {
        this.SSLRandomSeed = SSLRandomSeed;
    }
    
}
