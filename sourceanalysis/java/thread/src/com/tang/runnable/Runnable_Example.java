package com.tang.runnable;

/**
 * 
 * 使用Runnable创建线程
 * @author tangjiquan
 *
 */
public class Runnable_Example {
	
	public static void main(String[] args) {
		new Thread(new Printer("hello")).start();
	}
	
	
	
}
