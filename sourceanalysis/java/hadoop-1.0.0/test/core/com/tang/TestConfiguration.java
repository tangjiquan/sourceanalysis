package com.tang;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

public class TestConfiguration {
	
	@Test
	public void configuration(){
		Configuration conf = new Configuration();
		conf.addResource("configuration.xml");
		System.out.print(conf.get("weight"));
		
	}
}
