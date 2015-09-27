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

import org.apache.catalina.cluster.util.FastQueue;
import org.apache.catalina.cluster.util.LinkObject;
import org.apache.catalina.cluster.util.IQueue;

/**
 * Send cluster messages from a Message queue with only one socket. Ack and keep
 * Alive Handling is supported. Fast Queue can limit queue size and consume all messages at queue at one block.<br/>
 * Limit the queue lock contention under high load!<br/>
 * <ul>
 * <li>With autoConnect=false at ReplicationTransmitter, you can disconnect the
 * sender and all messages are queued. Only use this for small maintaince
 * isuses!</li>
 * <li>waitForAck=true, means that receiver ack the transfer</li>
 * <li>after one minute idle time, or number of request (100) the connection is
 * reconnected with next request. Change this for production use!</li>
 * <li>default ackTimeout is 15 sec: this is very low for big all session
 * replication messages after restart a node</li>
 * <li>disable keepAlive: keepAliveTimeout="-1" and
 * keepAliveMaxRequestCount="-1"</li>
 * <li>maxQueueLength: Limit the sender queue length (membership goes well, but transfer is failure!!)</li>
 * </ul>
 * FIXME: refactor code duplications with AsyncSocketSender => configurable or extract super class 
 * @author Peter Rossbach ( idea comes form Rainer Jung)
 * @version $Id: FastAsyncSocketSender.java 939539 2010-04-30 01:31:33Z kkolinko $
 * @since 5.5.9
 */
public class FastAsyncSocketSender extends DataSender {

    private static int threadCounter = 1;

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(FastAsyncSocketSender.class);

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "FastAsyncSocketSender/3.1";

    // ----------------------------------------------------- Instance Variables

    /**
     * Message Queue
     */
    private FastQueue queue = new FastQueue();

    /**
     * Active thread to push messages asynchronous to the other replication node
     */
    private FastQueueThread queueThread = null;

    /**
     * recover timeout ( default 5 secs)
     */
    private long recoverTimeout = 5000;
    
    /**
     * number of recover tries
     */
    private int recoverCounter = 0;

    /**
     * Count number of queue message
     */
    private long inQueueCounter = 0;

    /**
     * Count all successfull push messages from queue
     */
    private long outQueueCounter = 0;

    private int threadPriority = Thread.NORM_PRIORITY;;

    /**
     * Separate mutex for our local state.
     * We keep the synchronization independent of the synchronization
     * in the super class DataSender.
     */
    private final Object mutex = new Object();

    // ------------------------------------------------------------- Constructor

    /**
     * start background thread to push incomming cluster messages to replication
     * node
     * 
     * @param domain replication cluster domain (session domain)
     * @param host replication node tcp address
     * @param port replication node tcp port
     */
    public FastAsyncSocketSender(String domain,InetAddress host, int port) {
        super(domain,host, port);
        checkThread();
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
 

    /**
     * get current add wait timeout 
     * @return current wait timeout
     */
    public long getQueueAddWaitTimeout() {
        
        return queue.getAddWaitTimeout();
    }

    /**
     * Set add wait timeout (default 10000 msec)
     * @param timeout
     */
    public void setQueueAddWaitTimeout(long timeout) {
        queue.setAddWaitTimeout(timeout);
    }

    /**
     * get current remove wait timeout
     * @return The timeout
     */
    public long getQueueRemoveWaitTimeout() {
        return queue.getRemoveWaitTimeout();
    }

    /**
     * set remove wait timeout ( default 30000 msec)
     * @param timeout
     */
    public void setRemoveWaitTimeout(long timeout) {
        queue.setRemoveWaitTimeout(timeout);
    }

    /**
     * @return Returns the checkLock.
     */
    public boolean isQueueCheckLock() {
        return queue.isCheckLock();
    }
    /**
     * @param checkLock The checkLock to set.
     */
    public void setQueueCheckLock(boolean checkLock) {
        queue.setCheckLock(checkLock);
    }
    /**
     * @return Returns the doStats.
     */
    public boolean isQueueDoStats() {
        return queue.isDoStats();
    }
    /**
     * @param doStats The doStats to set.
     */
    public void setQueueDoStats(boolean doStats) {
        queue.setDoStats(doStats);
    }
    /**
     * @return Returns the timeWait.
     */
    public boolean isQueueTimeWait() {
        return queue.isTimeWait();
    }
    /**
     * @param timeWait The timeWait to set.
     */
    public void setQueueTimeWait(boolean timeWait) {
        queue.setTimeWait(timeWait);
    }
        
    /**
     * @return Returns the inQueueCounter.
     */
    public int getMaxQueueLength() {
        return queue.getMaxQueueLength();
    }

    /**
     * @param length max queue length
     */
    public void setMaxQueueLength(int length) {
        queue.setMaxQueueLength(length);
    }

    /**
     * @return Returns the add wait times.
     */
    public long getQueueAddWaitTime() {
        return queue.getAddWait();
    }

    /**
     * @return Returns the add wait times.
     */
    public long getQueueRemoveWaitTime() {
        return queue.getRemoveWait();
    }

    /**
     * @return Returns the inQueueCounter.
     */
    public long getInQueueCounter() {
        return inQueueCounter;
    }

    /**
     * @return Returns the outQueueCounter.
     */
    public long getOutQueueCounter() {
        return outQueueCounter;
    }

    /**
     * @return Returns the queueSize.
     */
    public int getQueueSize() {
        return queue.getSize();
    }

    /**
     * get current push message recover timeout 
     * @return current push message recover timeout
     */
    public long getRecoverTimeout() {
        
        return recoverTimeout;
    }

    /**
     * Set recover timeout (default 5000 msec)
     * @param timeout
     */
    public void setRecoverTimeout(long timeout) {
        recoverTimeout = timeout;
    }

    /**
     * get current push message recover counter 
     * @return current push message recover counter
     */
    public int getRecoverCounter() {
        
        return recoverCounter;
    }

    /**
     * Set recover couner (default 5 )
     * @param counter
     */
    public void setRecoverCounter(int counter) {
        recoverCounter = counter;
    }

    /**
     * change active the queue Thread priority 
     * @param threadPriority value must be between MIN and MAX Thread Priority
     * @exception IllegalArgumentException
     */
    public void setThreadPriority(int threadPriority) {
        if (log.isDebugEnabled())
            log.debug(sm.getString("FastAsyncSocketSender.setThreadPriority",
                    getAddress().getHostAddress(), new Integer(getPort()),
                    new Integer(threadPriority)));
        if (threadPriority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(sm.getString(
                    "FastAsyncSocketSender.min.exception", getAddress()
                            .getHostAddress(), new Integer(getPort()),
                    new Integer(threadPriority)));
        } else if (threadPriority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(sm.getString(
                    "FastAsyncSocketSender.max.exception", getAddress()
                            .getHostAddress(), new Integer(getPort()),
                    new Integer(threadPriority)));
        }
        this.threadPriority = threadPriority;
        if (queueThread != null)
            queueThread.setPriority(threadPriority);
    }

    /**
     * Get the current threadPriority
     * @return The thread priority
     */
    public int getThreadPriority() {
        return threadPriority;
    }

    /**
     * @return Returns the queuedNrOfBytes.
     */
    public long getQueuedNrOfBytes() {
        if(queueThread != null)
            return queueThread.getQueuedNrOfBytes();
        return 0l ;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Connect to socket and start background thread to push queued messages
     * 
     * @see org.apache.catalina.cluster.tcp.IDataSender#connect()
     */
    public void connect() throws java.io.IOException {
        super.connect();
        checkThread();
        if(!queue.isEnabled())
            queue.start() ;
    }

    /**
     * Disconnect socket ad stop queue thread
     * 
     * @see org.apache.catalina.cluster.tcp.IDataSender#disconnect()
     */
    public void disconnect() {
        stopThread();
        // delete "remove" lock at queue
        queue.stop() ;
        // enable that sendMessage can add new messages
        queue.start() ;
        // close socket
        super.disconnect();
    }

    /**
     * Send message to queue for later sending.
     * 
     * @see org.apache.catalina.cluster.tcp.DataSender#pushMessage(ClusterData)
     */
    public void sendMessage(ClusterData data)
            throws java.io.IOException {
        queue.add(data.getUniqueId(), data);
        synchronized (mutex) {
            inQueueCounter++;
            if(queueThread != null)
                queueThread.incQueuedNrOfBytes(data.getMessage().length);
        }
        if (log.isTraceEnabled())
            log.trace(sm.getString("AsyncSocketSender.queue.message",
                    getAddress().getHostAddress(), new Integer(getPort()), data.getUniqueId(), new Long(
                            data.getMessage().length)));
    }

    /**
     * Reset sender statistics
     */
    public synchronized void resetStatistics() {
        super.resetStatistics();
        inQueueCounter = queue.getSize();
        outQueueCounter = 0;
        queue.resetStatistics();
    }

    /**
     * Name of this SockerSender
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("FastAsyncSocketSender[");
        buf.append(getAddress().getHostAddress()).append(":").append(getPort()).append("]");
        return buf.toString();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Start Queue thread as daemon
     */
    protected void checkThread() {
        if (queueThread == null) {
            if (log.isInfoEnabled())
                log.info(sm.getString("AsyncSocketSender.create.thread",
                        getAddress(), new Integer(getPort())));
            queueThread = new FastQueueThread(this, queue);
            queueThread.setDaemon(true);
            queueThread.setPriority(getThreadPriority());
            queueThread.start();
        }
    }

    /**
     * stop queue worker thread
     */
    protected void stopThread() {
        if (queueThread != null) {
            queueThread.stopRunning();
            queueThread = null;
        }
    }

    // -------------------------------------------------------- Inner Class

    private class FastQueueThread extends Thread {

        
        /**
         * Sender queue
         */
        private IQueue queue = null;

        /**
         * Active sender
         */
        private FastAsyncSocketSender sender = null;

        /**
         * Thread is active
         */
        private boolean keepRunning = true;

        /**
         * Current number of bytes from all queued messages
         */
        private long queuedNrOfBytes = 0;

       

        /**
         * Only use inside FastAsyncSocketSender
         * @param sender
         * @param queue
         */
        private FastQueueThread(FastAsyncSocketSender sender, IQueue queue) {
            setName("Cluster-FastAsyncSocketSender-" + (threadCounter++));
            this.queue = queue;
            this.sender = sender;
        }
        
        /**
         * @return Returns the queuedNrOfBytes.
         */
        public long getQueuedNrOfBytes() {
            return queuedNrOfBytes;
        }
        
        protected void setQueuedNrOfBytes(long queuedNrOfBytes) {
            this.queuedNrOfBytes = queuedNrOfBytes;
        }

        protected void incQueuedNrOfBytes(long size) {
            queuedNrOfBytes += size;
        }
        
        protected void decQueuedNrOfBytes(long size) {
            queuedNrOfBytes -= size;
        }


        /**
         * Stop backend thread!
         */
         public void stopRunning() {
            keepRunning = false;
        }
        
        
        /**
         * Get the objects from queue and send all mesages to the sender.
         * @see java.lang.Runnable#run()
         */
        public void run() {
            while (keepRunning) {
                LinkObject entry = getQueuedMessage();
                if (entry != null) {
                    pushQueuedMessages(entry);
                } else {
                    if (keepRunning) {
                        log.warn(sm.getString("AsyncSocketSender.queue.empty",
                                sender.getAddress(), new Integer(sender
                                        .getPort())));
                    }
                }
            }
        }

        /**
         * Get List of queue cluster messages
         * @return list of cluster messages
         */
        protected LinkObject getQueuedMessage() {
            // get a link list of all queued objects
            if (log.isTraceEnabled())
                log.trace("Queuesize before=" + ((FastQueue) queue).getSize());
            LinkObject entry = queue.remove();
            if (log.isTraceEnabled())
                log.trace("Queuesize after=" + ((FastQueue) queue).getSize());
            return entry;
        }

        /**
         * Push all messages from queue to other nodes. Is revovery configured
         * make a resends with some waits.
         * @param entry list of messages
         */
        protected void pushQueuedMessages(LinkObject entry) {
            do {
                int messagesize = 0;
                ClusterData data = null ;
                try {
                    data = (ClusterData) entry.data();
                    messagesize = data.getMessage().length;
                    sender.pushMessage(data);
                } catch (Exception x) {
                    long rTimeout = sender.getRecoverTimeout() ;
                    int rCounter = sender.getRecoverCounter() ;
                    if(data != null && 
                       rTimeout > 0 && 
                       rCounter > 0) {
                        // wait that network get stabler
                        int counter = 1;
                        boolean success = false ;
                        do {
                            try {
                                Thread.sleep(rTimeout*counter);
                            } catch (Exception sleep) {
                            }
                            try {
                                if(log.isDebugEnabled()) {
                                    log.debug(sm.getString("AsyncSocketSender.send.recover",
                                            entry.getKey(),
                                            new Integer(counter), 
                                            new Integer(rCounter), new Long(rTimeout))) ;
                                }
                                sender.pushMessage(data);
                                success = true;
                            } catch (Exception xx) {
                              counter++; 
                            }
                        } while (keepRunning && !success && counter <= rCounter);
                        
                        if(!success) {
                            log.warn(sm.getString(
                                    "AsyncSocketSender.send.error", entry
                                    .getKey()), x);
                        }
                    } else {
                        log.warn(sm.getString(
                                "AsyncSocketSender.send.error", entry
                                .getKey()), x);
                    }
               } finally {
                    synchronized (sender.mutex) {
                        outQueueCounter++;
                        decQueuedNrOfBytes(messagesize);
                    }
                }
                entry = entry.next();
            } while (keepRunning && entry != null);
        }

    }

}
