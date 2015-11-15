package com.tang.example1;

public class Main {
	public static void main(String[] args) {
		System.out.println("Test gate start ........");
		Gate gate = new Gate();
		new UserThread(gate, "1zhangsan", "1shanghai").start();
		new UserThread(gate, "2lisi", "2beijing").start();
		new UserThread(gate, "3wangwu", "3guangdong").start();
	}
}
