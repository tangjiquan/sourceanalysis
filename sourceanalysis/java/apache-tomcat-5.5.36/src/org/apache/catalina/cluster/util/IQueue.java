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
 * A queue interface<BR>
 *
 * @author Rainer Jung
 * @author Peter Rossbach
 * @version $Id: IQueue.java 939539 2010-04-30 01:31:33Z kkolinko $
 */

public interface IQueue {

    public LinkObject remove();
    public boolean add(String key,Object data);
    public int getMaxQueueLength();
    public void setMaxQueueLength(int length);
    public void start();
    public void stop();
}
