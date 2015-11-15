package com.tang.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSlf4j {
	public static final Logger logger = LoggerFactory.getLogger(TestSlf4j.class);
	public static void main(String[] args) {
		logger.info("{}", "mamama");
	}
}
