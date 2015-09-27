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

package org.apache.catalina.tribes.membership;

import java.util.Properties;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.catalina.tribes.util.UUIDGenerator;
import java.io.IOException;

/**
 * A <b>membership</b> implementation using simple multicast.
 * This is the representation of a multicast membership service.
 * This class is responsible for maintaining a list of active cluster nodes in the cluster.
 * If a node fails to send out a heartbeat, the node will be dismissed.
 *
 * @author Filip Hanik
 * @version $Id: McastService.java 939539 2010-04-30 01:31:33Z kkolinko $
 */


public class McastService implements MembershipService,MembershipListener {

    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( McastService.class );

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "McastService/2.1";

    /**
     * The implementation specific properties
     */
    protected Properties properties = new Properties();
    /**
     * A handle to the actual low level implementation
     */
    protected McastServiceImpl impl;
    /**
     * A membership listener delegate (should be the cluster :)
     */
    protected MembershipListener listener;
    /**
     * The local member
     */
    protected MemberImpl localMember ;
    private int mcastSoTimeout;
    private int mcastTTL;
    
    protected byte[] payload;
    
    protected byte[] domain;

    /**
     * Create a membership service.
     */
    public McastService() {
        //default values
        properties.setProperty("mcastPort","45564");
        properties.setProperty("mcastAddress","228.0.0.4");
        properties.setProperty("memberDropTime","3000");
        properties.setProperty("mcastFrequency","500");

    }

    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }
    
    /**
     *
     * @param properties
     * <BR/>All are required<BR />
     * 1. mcastPort - the port to listen to<BR>
     * 2. mcastAddress - the mcast group address<BR>
     * 4. bindAddress - the bind address if any - only one that can be null<BR>
     * 5. memberDropTime - the time a member is gone before it is considered gone.<BR>
     * 6. mcastFrequency - the frequency of sending messages<BR>
     * 7. tcpListenPort - the port this member listens to<BR>
     * 8. tcpListenHost - the bind address of this member<BR>
     * @exception java.lang.IllegalArgumentException if a property is missing.
     */
    public void setProperties(Properties properties) {
        hasProperty(properties,"mcastPort");
        hasProperty(properties,"mcastAddress");
        hasProperty(properties,"memberDropTime");
        hasProperty(properties,"mcastFrequency");
        hasProperty(properties,"tcpListenPort");
        hasProperty(properties,"tcpListenHost");
        this.properties = properties;
    }

    /**
     * Return the properties, see setProperties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Return the local member name
     */
    public String getLocalMemberName() {
        return localMember.toString() ;
    }
 
    /**
     * Return the local member
     */
    public Member getLocalMember(boolean alive) {
        if ( alive && localMember != null && impl != null) localMember.setMemberAliveTime(System.currentTimeMillis()-impl.getServiceStartTime());
        return localMember;
    }
    
    /**
     * Sets the local member properties for broadcasting
     */
    public void setLocalMemberProperties(String listenHost, int listenPort) {
        properties.setProperty("tcpListenHost",listenHost);
        properties.setProperty("tcpListenPort",String.valueOf(listenPort));
        try {
            if (localMember != null) {
                localMember.setHostname(listenHost);
                localMember.setPort(listenPort);
            } else {
                localMember = new MemberImpl(listenHost, listenPort, 0);
                localMember.setUniqueId(UUIDGenerator.randomUUID(true));
                localMember.setPayload(getPayload());
                localMember.setDomain(getDomain());
            }
            localMember.getData(true, true);
        }catch ( IOException x ) {
            IllegalArgumentException iae = new IllegalArgumentException();
            iae.initCause(x);
            throw iae;
        }
    }
    
    public void setMcastAddr(String addr) {
        properties.setProperty("mcastAddress", addr);
    }

    public String getMcastAddr() {
        return properties.getProperty("mcastAddress");
    }

    public void setMcastBindAddress(String bindaddr) {
        properties.setProperty("mcastBindAddress", bindaddr);
    }

    public String getMcastBindAddress() {
        return properties.getProperty("mcastBindAddress");
    }

    public void setMcastPort(int port) {
        properties.setProperty("mcastPort", String.valueOf(port));
    }

    public int getMcastPort() {
        String p = properties.getProperty("mcastPort");
        return new Integer(p).intValue();
    }
    
    public void setMcastFrequency(long time) {
        properties.setProperty("mcastFrequency", String.valueOf(time));
    }

    public long getMcastFrequency() {
        String p = properties.getProperty("mcastFrequency");
        return new Long(p).longValue();
    }

    public void setMcastDropTime(long time) {
        properties.setProperty("memberDropTime", String.valueOf(time));
    }

    public long getMcastDropTime() {
        String p = properties.getProperty("memberDropTime");
        return new Long(p).longValue();
    }

    /**
     * Check if a required property is available.
     * @param properties The set of properties
     * @param name The property to check for
     */
    protected void hasProperty(Properties properties, String name){
        if ( properties.getProperty(name)==null) throw new IllegalArgumentException("McastService:Required property \""+name+"\" is missing.");
    }

    /**
     * Start broadcasting and listening to membership pings
     * @throws java.lang.Exception if a IO error occurs
     */
    public void start() throws java.lang.Exception {
        start(MembershipService.MBR_RX);
        start(MembershipService.MBR_TX);
    }
    
    public void start(int level) throws java.lang.Exception {
        hasProperty(properties,"mcastPort");
        hasProperty(properties,"mcastAddress");
        hasProperty(properties,"memberDropTime");
        hasProperty(properties,"mcastFrequency");
        hasProperty(properties,"tcpListenPort");
        hasProperty(properties,"tcpListenHost");

        if ( impl != null ) {
            impl.start(level);
            return;
        }
        String host = getProperties().getProperty("tcpListenHost");
        int port = Integer.parseInt(getProperties().getProperty("tcpListenPort"));
        
        if ( localMember == null ) {
            localMember = new MemberImpl(host, port, 100);
            localMember.setUniqueId(UUIDGenerator.randomUUID(true));
        } else {
            localMember.setHostname(host);
            localMember.setPort(port);
            localMember.setMemberAliveTime(100);
        }
        if ( this.payload != null ) localMember.setPayload(payload);
        if ( this.domain != null ) localMember.setDomain(domain);
        localMember.setServiceStartTime(System.currentTimeMillis());
        java.net.InetAddress bind = null;
        if ( properties.getProperty("mcastBindAddress")!= null ) {
            bind = java.net.InetAddress.getByName(properties.getProperty("mcastBindAddress"));
        }
        int ttl = -1;
        int soTimeout = -1;
        if ( properties.getProperty("mcastTTL") != null ) {
            try {
                ttl = Integer.parseInt(properties.getProperty("mcastTTL"));
            } catch ( Exception x ) {
                log.error("Unable to parse mcastTTL="+properties.getProperty("mcastTTL"),x);
            }
        }
        if ( properties.getProperty("mcastSoTimeout") != null ) {
            try {
                soTimeout = Integer.parseInt(properties.getProperty("mcastSoTimeout"));
            } catch ( Exception x ) {
                log.error("Unable to parse mcastSoTimeout="+properties.getProperty("mcastSoTimeout"),x);
            }
        }

        impl = new McastServiceImpl((MemberImpl)localMember,Long.parseLong(properties.getProperty("mcastFrequency")),
                                    Long.parseLong(properties.getProperty("memberDropTime")),
                                    Integer.parseInt(properties.getProperty("mcastPort")),
                                    bind,
                                    java.net.InetAddress.getByName(properties.getProperty("mcastAddress")),
                                    ttl,
                                    soTimeout,
                                    this);
        
        impl.start(level);
		

    }

 
    /**
     * Stop broadcasting and listening to membership pings
     */
    public void stop(int svc) {
        try  {
            if ( impl != null && impl.stop(svc) ) impl = null;
        } catch ( Exception x)  {
            log.error("Unable to stop the mcast service, level:"+svc+".",x);
        }
    }


    /**
     * Return all the members by name
     */
    public String[] getMembersByName() {
        Member[] currentMembers = getMembers();
        String [] membernames ;
        if(currentMembers != null) {
            membernames = new String[currentMembers.length];
            for (int i = 0; i < currentMembers.length; i++) {
                membernames[i] = currentMembers[i].toString() ;
            }
        } else
            membernames = new String[0] ;
        return membernames ;
    }
 
    /**
     * Return the member by name
     */
    public Member findMemberByName(String name) {
        Member[] currentMembers = getMembers();
        for (int i = 0; i < currentMembers.length; i++) {
            if (name.equals(currentMembers[i].toString()))
                return currentMembers[i];
        }
        return null;
    }

    /**
     * has members?
     */
    public boolean hasMembers() {
       if ( impl == null || impl.membership == null ) return false;
       return impl.membership.hasMembers();
    }
    
    public Member getMember(Member mbr) {
        if ( impl == null || impl.membership == null ) return null;
        return impl.membership.getMember(mbr);
    }

    /**
     * Return all the members
     */
    public Member[] getMembers() {
        if ( impl == null || impl.membership == null ) return null;
        return impl.membership.getMembers();
    }
    /**
     * Add a membership listener, this version only supports one listener per service,
     * so calling this method twice will result in only the second listener being active.
     * @param listener The listener
     */
    public void setMembershipListener(MembershipListener listener) {
        this.listener = listener;
    }
    /**
     * Remove the membership listener
     */
    public void removeMembershipListener(){
        listener = null;
    }

    public void memberAdded(Member member) {
        if ( listener!=null ) listener.memberAdded(member);
    }

    /**
     * Callback from the impl when a new member has been received
     * @param member The member
     */
    public void memberDisappeared(Member member)
    {
        if ( listener!=null ) listener.memberDisappeared(member);
    }

    public int getMcastSoTimeout() {
        return mcastSoTimeout;
    }
    public void setMcastSoTimeout(int mcastSoTimeout) {
        this.mcastSoTimeout = mcastSoTimeout;
        properties.setProperty("mcastSoTimeout", String.valueOf(mcastSoTimeout));
    }
    public int getMcastTTL() {
        return mcastTTL;
    }

    public byte[] getPayload() {
        return payload;
    }
    
    public byte[] getDomain() {
        return domain;
    }

    public void setMcastTTL(int mcastTTL) {
        this.mcastTTL = mcastTTL;
        properties.setProperty("mcastTTL", String.valueOf(mcastTTL));
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        if ( localMember != null ) {
            localMember.setPayload(payload);
            localMember.getData(true,true);
            try {
                if (impl != null) impl.send(false);
            }catch ( Exception x ) {
                log.error("Unable to send payload update.",x);
            }
        }
    }
    
    public void setDomain(byte[] domain) {
        this.domain = domain;
        if ( localMember != null ) {
            localMember.setDomain(domain);
            localMember.getData(true,true);
            try {
                if (impl != null) impl.send(false);
            }catch ( Exception x ) {
                log.error("Unable to send domain update.",x);
            }
        }
    }

    /**
     * Simple test program
     * @param args Command-line arguments
     * @throws Exception If an error occurs
     */
    public static void main(String args[]) throws Exception {
		if(log.isInfoEnabled())
            log.info("Usage McastService hostname tcpport");
        McastService service = new McastService();
        java.util.Properties p = new java.util.Properties();
        p.setProperty("mcastPort","5555");
        p.setProperty("mcastAddress","224.10.10.10");
        p.setProperty("mcastClusterDomain","catalina");
        p.setProperty("bindAddress","localhost");
        p.setProperty("memberDropTime","3000");
        p.setProperty("mcastFrequency","500");
        p.setProperty("tcpListenPort","4000");
        p.setProperty("tcpListenHost","127.0.0.1");
        service.setProperties(p);
        service.start();
        Thread.sleep(60*1000*60);
    }
}
