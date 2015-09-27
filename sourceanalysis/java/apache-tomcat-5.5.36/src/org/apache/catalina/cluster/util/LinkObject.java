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

package org.apache.catalina.cluster.util;

/**
 * The class <b>LinkObject</b> implements an element
 * for a linked list, consisting of a general
 * data object and a pointer to the next element.
 *
 * @author Rainer Jung
 * @author Peter Rossbach
 * @version $Id: LinkObject.java 939539 2010-04-30 01:31:33Z kkolinko $

 */

public class LinkObject {

    private Object payload;
    private LinkObject next;
    private String key ;
    
    /**
     * Construct a new element from the data object.
     * Sets the pointer to null.
     *
     * @param key The key
     * @param payload The data object.
     */
    public LinkObject(String key,Object payload) {
        this.payload = payload;
        this.next = null;
        this.key = key ;
    }

    /**
     * Set the next element.
     * @param next The next element.
     */
    public void append(LinkObject next) {
        this.next = next;
    }

    /**
     * Get the next element.
     * @return The next element.
     */
    public LinkObject next() {
        return next;
    }

    /**
     * Get the data object from the element.
     * @return The data object from the element.
     */
    public Object data() {
        return payload;
    }

    /**
     * Get the unique message id
     * @return the unique message id
     */
    public Object getKey() {
        return key;
    }

}
