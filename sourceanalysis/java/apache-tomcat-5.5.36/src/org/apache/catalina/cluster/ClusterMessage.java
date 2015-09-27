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
package org.apache.catalina.cluster;

import java.io.Serializable;

/**
 * @author Peter Rossbach
 */
public interface ClusterMessage extends Serializable {
    
    public final static int FLAG_FORBIDDEN = 0 ;
    public final static int FLAG_ALLOWED = 1 ;
    public final static int FLAG_DEFAULT = 2 ;
    
    /**
     * Get the address that this message originated from.  This would be set
     * if the message was being relayed from a host other than the one
     * that originally sent it.
     */
    public Member getAddress();

    /**
     * Called by the cluster before sending it to the other
     * nodes.
     *
     * @param member Member
     */
    public void setAddress(Member member);

    /**
     * Timestamp message.
     *
     * @return long
     */
    public long getTimestamp();

    /**
     * Called by the cluster before sending out
     * the message.
     *
     * @param timestamp The timestamp
     */
    public void setTimestamp(long timestamp);

    /**
     * Each message must have a unique ID, in case of using async replication,
     * and a smart queue, this id is used to replace messages not yet sent.
     *
     * @return String
     */
    public String getUniqueId();

    /**
     * Each message can made the desicion that resend is allowed or not or handle by default.
     * @return 0 Forbidden, 1 allowed, 2 default
     * @since 5.5.10
     */
    public int getResend();
    
    /**
     * set desicion that resend is allowed or not or handle by default.
     *
     * @param resend 0 Forbidden, 1 allowed, 2 default
     * @since 5.5.10
     */
    public void setResend(int resend) ;

    /**
     * Each message can made the desicion that compress is allowed or not or handle by default.
     *
     * @return 0 Forbidden, 1 allowed, 2 default
     * @since 5.5.10
     */
    public int getCompress();
    
    /**
     * set desicion that compress is allowed or not or handle by default.
     *
     * @param compress 0 Forbidden, 1 allowed, 2 default
     * @since 5.5.10
     */
    public void setCompress(int compress) ;

}
