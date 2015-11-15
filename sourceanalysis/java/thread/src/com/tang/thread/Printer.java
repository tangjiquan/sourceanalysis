package com.tang.thread;

public class Printer extends Thread{
	private String message;
	public Printer(String message){
		this.message = message;
	}
	
	@Override
	public void run() {
		super.run();
		for(int i=0; i<100; i++){
			System.out.print(message);
		}
	}
}
