package com.tang.runnable;

public class Printer implements Runnable{

	private String message;
	private Object obj;
	public Printer(String message) {
		this.message = message;
	}
	@Override
	public void run() {
		for(int i=0; i<100; i++){
			System.out.print(message);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
