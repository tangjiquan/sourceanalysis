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


import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;


/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Cluster definition element.  </p>
 *
 * @author Filip Hanik
 * @author Peter Rossbach
 * @version $Id: ClusterRuleSet.java 939539 2010-04-30 01:31:33Z kkolinko $
 */

public class ClusterRuleSet extends RuleSetBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public ClusterRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public ClusterRuleSet(String prefix) {
        super();
        this.namespaceURI = null;
        this.prefix = prefix;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester) {
        //Cluster configuration start
        digester.addObjectCreate(prefix + "Membership",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Membership");
        digester.addSetNext(prefix + "Membership",
                            "setMembershipService",
                            "org.apache.catalina.cluster.MembershipService");

        digester.addObjectCreate(prefix + "Sender",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Sender");
        digester.addSetNext(prefix + "Sender",
                            "setClusterSender",
                            "org.apache.catalina.cluster.ClusterSender");

        digester.addObjectCreate(prefix + "Receiver",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Receiver");
        digester.addSetNext(prefix + "Receiver",
                            "setClusterReceiver",
                            "org.apache.catalina.cluster.ClusterReceiver");

        digester.addObjectCreate(prefix + "Valve",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Valve");
        digester.addSetNext(prefix + "Valve",
                            "addValve",
                            "org.apache.catalina.Valve");

        digester.addObjectCreate(prefix + "Deployer",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Deployer");
        digester.addSetNext(prefix + "Deployer",
                            "setClusterDeployer",
                            "org.apache.catalina.cluster.ClusterDeployer");

        digester.addObjectCreate(prefix + "Listener",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties(prefix + "Listener");
        digester.addSetNext(prefix + "Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate(prefix + "ClusterListener",
                null, // MUST be specified in the element
                "className");
        digester.addSetProperties(prefix + "ClusterListener");
        digester.addSetNext(prefix + "ClusterListener",
                            "addClusterListener",
                            "org.apache.catalina.cluster.MessageListener");
        //Cluster configuration end
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

}
