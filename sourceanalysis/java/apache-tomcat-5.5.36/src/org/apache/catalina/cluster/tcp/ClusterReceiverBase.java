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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.io.ListenCallback;
import org.apache.catalina.cluster.session.ClusterSessionListener;
import org.apache.catalina.cluster.session.ReplicationStream;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;

/**
* FIXME i18n log messages
* @author Peter Rossbach
* @version $Id: ClusterReceiverBase.java 939539 2010-04-30 01:31:33Z kkolinko $
*/

public abstract class ClusterReceiverBase implements Runnable, ClusterReceiver,ListenCallback {
    
    protected static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( ClusterReceiverBase.class );

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    private CatalinaCluster cluster;
    private java.net.InetAddress bind;
    private String tcpListenAddress;
    private int tcpListenPort;
    private boolean sendAck;
    protected boolean doListen = false;

    /**
     * total bytes to recevied
     */
    protected long totalReceivedBytes = 0;
    
    /**
     * doProcessingStats
     */
    protected boolean doReceivedProcessingStats = false;

    /**
     * proessingTime
     */
    protected long receivedProcessingTime = 0;
    
    /**
     * min proessingTime
     */
    protected long minReceivedProcessingTime = Long.MAX_VALUE ;

    /**
     * max proessingTime
     */
    protected long maxReceivedProcessingTime = 0;
    
    /**
     * Sending Stats
     */
    private long nrOfMsgsReceived = 0;

    private long receivedTime = 0;

    private long lastChecked = System.currentTimeMillis();

    private int rxBufSize = 43800;
    private int txBufSize = 25188;
    private boolean tcpNoDelay = true;
    private boolean soKeepAlive = false;
    private boolean ooBInline = true;
    private boolean soReuseAddress = true;
    private boolean soLingerOn = true;
    private int soLingerTime = 3;
    private int soTrafficClass = 0x04 | 0x08 | 0x010;
    private int timeout = -1; // Regression with older release, better set timeout at production env

    /**
     * Compress message data bytes
     */
    private boolean compress = true ;

    /**
     * Transmitter Mbean name
     */
    private ObjectName objectName;

    /**
     * @return the ooBInline
     */
    public boolean isOoBInline() {
        return ooBInline;
    }

    /**
     * @param ooBInline the ooBInline to set
     */
    public void setOoBInline(boolean ooBInline) {
        this.ooBInline = ooBInline;
    }

    /**
     * @return the rxBufSize
     */
    public int getRxBufSize() {
        return rxBufSize;
    }

    /**
     * @param rxBufSize the rxBufSize to set
     */
    public void setRxBufSize(int rxBufSize) {
        this.rxBufSize = rxBufSize;
    }

    /**
     * @return the soKeepAlive
     */
    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    /**
     * @param soKeepAlive the soKeepAlive to set
     */
    public void setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
    }

    /**
     * @return the soLingerOn
     */
    public boolean isSoLingerOn() {
        return soLingerOn;
    }

    /**
     * @param soLingerOn the soLingerOn to set
     */
    public void setSoLingerOn(boolean soLingerOn) {
        this.soLingerOn = soLingerOn;
    }

    /**
     * @return the soLingerTime
     */
    public int getSoLingerTime() {
        return soLingerTime;
    }

    /**
     * @param soLingerTime the soLingerTime to set
     */
    public void setSoLingerTime(int soLingerTime) {
        this.soLingerTime = soLingerTime;
    }

    /**
     * @return the soReuseAddress
     */
    public boolean isSoReuseAddress() {
        return soReuseAddress;
    }

    /**
     * @param soReuseAddress the soReuseAddress to set
     */
    public void setSoReuseAddress(boolean soReuseAddress) {
        this.soReuseAddress = soReuseAddress;
    }

    /**
     * @return the soTrafficClass
     */
    public int getSoTrafficClass() {
        return soTrafficClass;
    }

    /**
     * @param soTrafficClass the soTrafficClass to set
     */
    public void setSoTrafficClass(int soTrafficClass) {
        this.soTrafficClass = soTrafficClass;
    }

    /**
     * @return the tcpNoDelay
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * @param tcpNoDelay the tcpNoDelay to set
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the txBufSize
     */
    public int getTxBufSize() {
        return txBufSize;
    }

    /**
     * @param txBufSize the txBufSize to set
     */
    public void setTxBufSize(int txBufSize) {
        this.txBufSize = txBufSize;
    }
    
    /**
     * @return Returns the doListen.
     */
    public boolean isDoListen() {
        return doListen;
    }

    /**
     * @return Returns the bind.
     */
    public java.net.InetAddress getBind() {
        if (bind == null) {
            try {
                if (log.isDebugEnabled())
                    log.debug("Starting replication listener on address:"
                            + tcpListenAddress);
                bind = java.net.InetAddress.getByName(tcpListenAddress);
            } catch (IOException ioe) {
                log.error("Failed bind replication listener on address:"
                        + tcpListenAddress, ioe);
            }
        }
      return bind;
    }
    
    /**
     * @param bind The bind to set.
     */
    public void setBind(java.net.InetAddress bind) {
        this.bind = bind;
    }
    public void setCatalinaCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

    public CatalinaCluster getCatalinaCluster() {
        return (CatalinaCluster) cluster;
    }
    
    /**
     *  set Receiver ObjectName
     * 
     * @param name
     */
    public void setObjectName(ObjectName name) {
        objectName = name;
    }

    /**
     * Receiver ObjectName
     * 
     */
    public ObjectName getObjectName() {
        return objectName;
    }
    
    /**
     * @return Returns the compress.
     */
    public boolean isCompress() {
        return compress;
    }
    
    /**
     * @param compressMessageData The compress to set.
     */
    public void setCompress(boolean compressMessageData) {
        this.compress = compressMessageData;
    }
    
    /**
     * Send ACK to sender
     * 
     * @return True if sending ACK
     */
    public boolean isSendAck() {
        return sendAck;
    }

    /**
     * set ack mode or not!
     * 
     * @param sendAck
     */
    public void setSendAck(boolean sendAck) {
        this.sendAck = sendAck;
    }
 
    /**
     * get tcp listen recevier ip address
     * @return listen address
     */
    public String getTcpListenAddress() {
        return tcpListenAddress;
    }

    /**
     * Set TCP listen ip address. If arg auto use InetAddress.getLocalHost()
     * otherwise arg converted with InetAddress.getByName().
     * 
     * @param tcpListenAddress 
     */
    public void setTcpListenAddress(String tcpListenAddress) {
        try {
            if ("auto".equals(tcpListenAddress)) {
                this.tcpListenAddress = 
                    java.net.InetAddress.getLocalHost().getHostAddress();
            } else {
                this.tcpListenAddress =
                    java.net.InetAddress.getByName(tcpListenAddress).getHostAddress();
            }
            if (log.isDebugEnabled())
                log.debug("Set replication listener on address:"
                        + this.tcpListenAddress);
        } catch (IOException ioe) {
            log.error("Failed get Inet address at replication listener on address:"
                    + tcpListenAddress, ioe);
        }
    }

    
    public int getTcpListenPort() {
        return tcpListenPort;
    }
    
    public void setTcpListenPort(int tcpListenPort) {
        this.tcpListenPort = tcpListenPort;
    }
  
    public String getHost() {
        return getTcpListenAddress();
    }

    public int getPort() {
        return getTcpListenPort();
    }
    // ------------------------------------------------------------- stats

    /**
     * @return Returns the doReceivedProcessingStats.
     */
    public boolean isDoReceivedProcessingStats() {
        return doReceivedProcessingStats;
    }
    /**
     * @param doReceiverProcessingStats The doReceivedProcessingStats to set.
     */
    public void setDoReceivedProcessingStats(boolean doReceiverProcessingStats) {
        this.doReceivedProcessingStats = doReceiverProcessingStats;
    }
    /**
     * @return Returns the maxReceivedProcessingTime.
     */
    public long getMaxReceivedProcessingTime() {
        return maxReceivedProcessingTime;
    }
    /**
     * @return Returns the minReceivedProcessingTime.
     */
    public long getMinReceivedProcessingTime() {
        return minReceivedProcessingTime;
    }
    /**
     * @return Returns the receivedProcessingTime.
     */
    public long getReceivedProcessingTime() {
        return receivedProcessingTime;
    }
    /**
     * @return Returns the totalReceivedBytes.
     */
    public long getTotalReceivedBytes() {
        return totalReceivedBytes;
    }
    
    /**
     * @return Returns the avg receivedProcessingTime/nrOfMsgsReceived.
     */
    public double getAvgReceivedProcessingTime() {
        if ( nrOfMsgsReceived > 0 ) {
            return ((double)receivedProcessingTime) / nrOfMsgsReceived;
        } else {
            return 0;
        }
    }

    /**
     * @return Returns the avg totalReceivedBytes/nrOfMsgsReceived.
     */
    public long getAvgTotalReceivedBytes() {
        if ( nrOfMsgsReceived > 0 ) {
            return ((long)totalReceivedBytes) / nrOfMsgsReceived;
        } else {
            return 0;
        }
    }

    /**
     * @return Returns the receivedTime.
     */
    public long getReceivedTime() {
        return receivedTime;
    }

    /**
     * @return Returns the lastChecked.
     */
    public long getLastChecked() {
        return lastChecked;
    }

    /**
     * @return Returns the nrOfMsgsReceived.
     */
    public long getNrOfMsgsReceived() {
        return nrOfMsgsReceived;
    }

    /**
     * start cluster receiver
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#start()
     */
    public void start() {
        try {
            getBind();
            Thread t = new Thread(this, "ClusterReceiver");
            t.setDaemon(true);
            t.start();
        } catch (Exception x) {
            log.fatal("Unable to start cluster receiver", x);
        }
        registerReceiverMBean();
    }

 
    /**
     * Stop accept
     * 
     * @see org.apache.catalina.cluster.ClusterReceiver#stop()
     * @see #stopListening()
     */
    public void stop() {
        stopListening();
        unregisterRecevierMBean();
     
    }
    
    /**
     * Register Receiver MBean
     * &lt;domain&gt;:type=ClusterReceiver,host=&lt;host&gt;
     */
    protected void registerReceiverMBean() {
        if (cluster != null && cluster instanceof SimpleTcpCluster) {
            SimpleTcpCluster scluster = (SimpleTcpCluster) cluster;
            ObjectName clusterName = scluster.getObjectName();
            try {
                MBeanServer mserver = scluster.getMBeanServer();
                Container container = cluster.getContainer();
                String name = clusterName.getDomain() + ":type=ClusterReceiver";
                if (container instanceof StandardHost) {
                    name += ",host=" + clusterName.getKeyProperty("host");
                }
                ObjectName receiverName = new ObjectName(name);
                if (mserver.isRegistered(receiverName)) {
                    if (log.isWarnEnabled())
                        log.warn(sm.getString(
                                "cluster.mbean.register.already",
                                receiverName));
                    return;
                }
                setObjectName(receiverName);
                mserver.registerMBean(scluster.getManagedBean(this),
                        getObjectName());
            } catch (Exception e) {
                log.warn(e);
            }
        }
    }
   
    /**
     * UnRegister Recevier MBean
     * &lt;domain&gt;:type=ClusterReceiver,host=&lt;host&gt;
     */
    protected void unregisterRecevierMBean() {
        if (cluster != null && getObjectName() != null
                && cluster instanceof SimpleTcpCluster) {
            SimpleTcpCluster scluster = (SimpleTcpCluster) cluster;
            try {
                MBeanServer mserver = scluster.getMBeanServer();
                mserver.unregisterMBean(getObjectName());
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * stop Listener sockets
     */
    protected abstract void stopListening() ;

    /**
     * Start Listener
     * @throws Exception
     */
    protected abstract void listen ()
       throws Exception ;

    
    /**
     * Start thread and listen
     */
    public void run()
    {
        try
        {
            listen();
        }
        catch ( Exception x )
        {
            log.error("Unable to start cluster listener.",x);
        }
    }

    // --------------------------------------------------------- receiver messages

    /**
     * receiver Message from other node.
     * All SessionMessage forward to ClusterManager and other message dispatch to all accept MessageListener.
     *
     * @see ClusterSessionListener#messageReceived(ClusterMessage)
     */
    public void messageDataReceived(ClusterData data) {
    //public void messageDataReceived(byte[] data) {
        long timeSent = 0 ;
        if (doReceivedProcessingStats) {
            timeSent = System.currentTimeMillis();
        }
        try {
            ClusterMessage message = deserialize(data);
            cluster.receive(message);
        } catch (Exception x) {
            log
                    .error(
                            "Unable to deserialize session message or unexpected exception from message listener.",
                            x);
        } finally {
            if (doReceivedProcessingStats) {
                addReceivedProcessingStats(timeSent);
            }
        }
    }

    /**
     * deserialize the receieve cluster message
     * @param data uncompress data
     * @return The message
     * @throws IOException
     * @throws ClassNotFoundException
     */
    //protected ClusterMessage deserialize(byte[] data)
    protected ClusterMessage deserialize(ClusterData data)
            throws IOException, ClassNotFoundException {
        Object message = null;
        if (data != null) {
            InputStream instream;
            if (isCompress() || data.getCompress() == ClusterMessage.FLAG_ALLOWED ) {
                instream = new GZIPInputStream(new ByteArrayInputStream(data.getMessage()));
            } else {
                instream = new ByteArrayInputStream(data.getMessage());
            }
            ReplicationStream stream = new ReplicationStream(instream,
                    getClass().getClassLoader());
            message = stream.readObject();
            // calc stats really received bytes
            totalReceivedBytes += data.getMessage().length;
            //totalReceivedBytes += data.length;
            nrOfMsgsReceived++;
            instream.close();
        }
        if (message instanceof ClusterMessage)
            return (ClusterMessage) message;
        else {
            if (log.isDebugEnabled())
                log.debug("Message " + message.toString() + " from type "
                        + message.getClass().getName()
                        + " transfered but is not a cluster message");
            return null;
        }
    }
    
    // --------------------------------------------- Performance Stats

    /**
     * Reset sender statistics
     */
    public synchronized void resetStatistics() {
        nrOfMsgsReceived = 0;
        totalReceivedBytes = 0;
        minReceivedProcessingTime = Long.MAX_VALUE ;
        maxReceivedProcessingTime = 0 ;
        receivedProcessingTime = 0 ;
        receivedTime = 0 ;
    }

    /**
     * Add receiver processing stats times
     * @param startTime
     */
    protected void addReceivedProcessingStats(long startTime) {
        long current = System.currentTimeMillis() ;
        long time = current - startTime ;
        synchronized(this) {
            if(time < minReceivedProcessingTime)
                minReceivedProcessingTime = time ;
            if( time > maxReceivedProcessingTime)
                maxReceivedProcessingTime = time ;
            receivedProcessingTime += time ;
        }
        if (log.isDebugEnabled()) {
            if ((current - lastChecked) > 5000) {
                log.debug("Calc msg send time total=" + receivedTime
                        + "ms num request=" + nrOfMsgsReceived
                        + " average per msg="
                        + (receivedTime / nrOfMsgsReceived) + "ms.");
                lastChecked=current ;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.catalina.cluster.io.ListenCallback#sendAck()
     */
    public void sendAck() throws IOException {
        // do nothing
    }

}
