package com.tang.test;

import org.apache.zookeeper.server.quorum.QuorumPeerMain;

public class StartZookeeperServer {
	
	public static void main(String[] args) {
		QuorumPeerMain.main(new String[]{"E:/workspace/git/sources/sourceanalysis/sourceanalysis/java/zookeeper-3.4.6/build/classes/zoo.cfg"});
	}
}
