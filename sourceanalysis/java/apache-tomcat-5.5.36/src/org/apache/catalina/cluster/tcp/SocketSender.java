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

package org.apache.catalina.cluster.tcp;

import java.net.InetAddress;

/**
 * Send cluster messages sync to request with only one socket.
 * 
 * @author Filip Hanik
 * @author Peter Rossbach
 * @version 1.2
 */

public class SocketSender extends DataSender {
    // ----------------------------------------------------- Instance Variables

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "SocketSender/1.2";

    // ------------------------------------------------------------- Constructor
   
   /**
    * @param domain replication cluster domain (session domain)
    * @param host replication node tcp address
    * @param port replication node tcp port
    */
    public SocketSender(String domain,InetAddress host, int port) {
        super(domain,host, port);
    }

    public SocketSender(String domain,InetAddress host, int port, SenderState state) {
        super(domain,host, port, state);
    }

    // ------------------------------------------------------------- Properties

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }

    public String toString() {
        StringBuffer buf = new StringBuffer("SocketSender[");
        buf.append(getAddress()).append(":").append(getPort()).append("]");
        return buf.toString();
    }

}
