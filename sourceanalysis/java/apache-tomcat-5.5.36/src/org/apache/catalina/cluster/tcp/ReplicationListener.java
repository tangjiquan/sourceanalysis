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


import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.catalina.cluster.io.ObjectReader;

/**
* FIXME i18n log messages
* FIXME jmx support
* @author Peter Rossbach
* @author Filip Hanik
* @version $Id: ReplicationListener.java 939539 2010-04-30 01:31:33Z kkolinko $
*/
public class ReplicationListener extends ClusterReceiverBase
{

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "ReplicationListener/1.2";
    
    private ThreadPool pool = null;
    private int tcpThreadCount;
    private long tcpSelectorTimeout;    
    private Selector selector = null;
    
    private Object interestOpsMutex = new Object();
    
    public ReplicationListener() {
    }
    
    /**
     * Return descriptive information about this implementation and the
     * corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }
 
    public long getTcpSelectorTimeout() {
        return tcpSelectorTimeout;
    }
    public void setTcpSelectorTimeout(long tcpSelectorTimeout) {
        this.tcpSelectorTimeout = tcpSelectorTimeout;
    }
    public int getTcpThreadCount() {
        return tcpThreadCount;
    }
    public void setTcpThreadCount(int tcpThreadCount) {
        this.tcpThreadCount = tcpThreadCount;
    }
    public Object getInterestOpsMutex() {
        return interestOpsMutex;
    }

    /**
     * start cluster receiver
     * @throws Exception
     * @see org.apache.catalina.cluster.ClusterReceiver#start()
     */
    public void start() {
            try {
                pool = new ThreadPool(tcpThreadCount, TcpReplicationThread.class, interestOpsMutex);
            } catch (Exception e) {
                log.error("ThreadPool can initilzed. Listener not started",e);
                return ;
            }
            super.start() ;
     }
    

    /**
     * get data from channel and store in byte array
     * send it to cluster
     * @throws IOException
     * @throws java.nio.channels.ClosedChannelException
     */
    protected void listen ()
        throws Exception
    {
        if (doListen) {
            log.warn("ServerSocketChannel allready started");
            return;
        }
        doListen=true;
        // allocate an unbound server socket channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // Get the associated ServerSocket to bind it with
        ServerSocket serverSocket = serverChannel.socket();
        // create a new Selector for use below
        selector = Selector.open();
        // set the port the server channel will listen to
        serverSocket.bind (new InetSocketAddress (getBind(),getTcpListenPort()));
        // set non-blocking mode for the listening socket
        serverChannel.configureBlocking (false);
        // register the ServerSocketChannel with the Selector
        serverChannel.register (selector, SelectionKey.OP_ACCEPT);
        
        while (doListen && selector != null) {
            // this may block for a long time, upon return the
            // selected set contains keys of the ready channels
            try {

                int n = selector.select(tcpSelectorTimeout);
                if (n == 0) {
                    //there is a good chance that we got here 
                    //because the TcpReplicationThread called
                    //selector wakeup().
                    //if that happens, we must ensure that that
                    //thread has enough time to call interestOps
                    //synchronized (interestOpsMutex) {
                        //if we got the lock, means there are no
                        //keys trying to register for the 
                        //interestOps method
                    //}
                    continue; // nothing to do
                }
                // get an iterator over the set of selected keys
                Iterator it = selector.selectedKeys().iterator();
                // look at each key in the selected set
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    // Is a new connection coming in?
                    if (key.isAcceptable()) {
                        ServerSocketChannel server =
                            (ServerSocketChannel) key.channel();
                        // get channel
                        SocketChannel channel = server.accept();
                        channel.socket().setReceiveBufferSize(getRxBufSize());
                        channel.socket().setSendBufferSize(getTxBufSize());
                        channel.socket().setTcpNoDelay(isTcpNoDelay());
                        channel.socket().setKeepAlive(isSoKeepAlive());
                        channel.socket().setOOBInline(isOoBInline());
                        channel.socket().setReuseAddress(isSoReuseAddress());
                        channel.socket().setSoLinger(isSoLingerOn(),getSoLingerTime());
                        channel.socket().setTrafficClass(getSoTrafficClass());
                        if(getTimeout() > -1)
                            channel.socket().setSoTimeout(getTimeout());
                        // attach Reader and register channel
                        Object attach = new ObjectReader(channel, selector,
                                    this) ;
                        registerChannel(selector,
                                        channel,
                                        SelectionKey.OP_READ,
                                        attach);
                    }
                    // is there data to read on this channel?
                    if (key.isReadable()) {
                        readDataFromSocket(key);
                    } else {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                    }

                    // remove key from selected set, it's been handled
                    it.remove();
                }
            } catch (java.nio.channels.ClosedSelectorException cse) {
                // ignore is normal at shutdown or stop listen socket
            } catch (java.nio.channels.CancelledKeyException nx) {
                log.warn(
                    "Replication client disconnected, error when polling key. Ignoring client.");
            } catch (Exception x) {
                log.error("Unable to process request in ReplicationListener", x);
            }

        }
        serverChannel.close();
        if(selector != null)
            selector.close();
    }

    /**
     * Close Selector.
     *
     * @see org.apache.catalina.cluster.tcp.ClusterReceiverBase#stopListening()
     */
    protected void stopListening() {
        // Bugzilla 37529: http://issues.apache.org/bugzilla/show_bug.cgi?id=37529
        doListen = false;
        if ( selector != null ) {
            try {
                for(int i = 0; i < getTcpThreadCount(); i++) {
                    selector.wakeup();
                }
                selector.close();
            } catch ( Exception x ) {
                log.error("Unable to close cluster receiver selector.",x);
            } finally {
                selector = null;                
            }
        }
   }
        
    // ----------------------------------------------------------

    /**
     * Register the given channel with the given selector for
     * the given operations of interest
     */
    protected void registerChannel (Selector selector,
                                    SelectableChannel channel,
                                    int ops,
                                    Object attach)
    throws Exception {
        if (channel == null) return; // could happen
        // set the new channel non-blocking
        channel.configureBlocking (false);
        // register it with the selector
        channel.register (selector, ops, attach);
    }

    // ----------------------------------------------------------

    /**
     * Sample data handler method for a channel with data ready to read.
     * @param key A SelectionKey object associated with a channel
     *  determined by the selector to be ready for reading.  If the
     *  channel returns an EOF condition, it is closed here, which
     *  automatically invalidates the associated key.  The selector
     *  will then de-register the channel on the next select call.
     */
    protected void readDataFromSocket (SelectionKey key)
        throws Exception
    {
        TcpReplicationThread worker = (TcpReplicationThread)pool.getWorker();
        if (worker == null) {
            // No threads available, do nothing, the selection
            // loop will keep calling this method until a
            // thread becomes available.
            // FIXME: This design could be improved.
            if(log.isDebugEnabled())
                log.debug("No TcpReplicationThread available");
        } else {
            // invoking this wakes up the worker thread then returns
            worker.serviceChannel(key, isSendAck());
        }
    }

    
}
