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


package org.apache.catalina.cluster.session;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * Custom subclass of <code>ObjectInputStream</code> that loads from the
 * class loader for this web application.  This allows classes defined only
 * with the web application to be found correctly.
 *
 * @author Craig R. McClanahan
 * @author Bip Thelin
 * @version $Id: ReplicationStream.java 1158254 2011-08-16 13:03:26Z markt $
 */

public final class ReplicationStream extends ObjectInputStream {


    /**
     * The class loader we will use to resolve classes.
     */
    private ClassLoader classLoader = null;

    /**
     * Construct a new instance of CustomObjectInputStream
     *
     * @param stream The input stream we will read from
     * @param classLoader The class loader used to instantiate objects
     *
     * @exception IOException if an input/output error occurs
     */
    public ReplicationStream(InputStream stream,
                             ClassLoader classLoader)
        throws IOException {

        super(stream);
        this.classLoader = classLoader;
    }

    /**
     * Load the local class equivalent of the specified stream class
     * description, by using the class loader assigned to this Context.
     *
     * @param classDesc Class description from the input stream
     *
     * @exception ClassNotFoundException if this class cannot be found
     * @exception IOException if an input/output error occurs
     */
    public Class resolveClass(ObjectStreamClass classDesc)
        throws ClassNotFoundException, IOException {
        String name = classDesc.getName();
        boolean tryRepFirst = name.startsWith("org.apache.catalina.cluster");
        try {
            try
            {
                if ( tryRepFirst ) return findReplicationClass(name);
                else return findWebappClass(name);
            }
            catch ( Exception x )
            {
                if ( tryRepFirst ) return findWebappClass(name);
                else return findReplicationClass(name);
            }
        } catch (ClassNotFoundException e) {
            return super.resolveClass(classDesc);
        }
    }
    
    /**
     * ObjectInputStream.resolveProxyClass has some funky way of using 
     * the incorrect class loader to resolve proxy classes, let's do it our way instead
     */
    protected Class resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException {
        
        ClassLoader latestLoader = classLoader;
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;

        // define proxy in class loader of non-public interface(s), if any
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = this.findWebappClass(interfaces[i]);
            if (latestLoader == null) latestLoader = cl.getClassLoader();
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                            "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader
                    : latestLoader, classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    public Class findReplicationClass(String name)
        throws ClassNotFoundException, IOException {
        return Class.forName(name, false, getClass().getClassLoader());
    }

    public Class findWebappClass(String name)
        throws ClassNotFoundException, IOException {
        return Class.forName(name, false, classLoader);
    }


}
