package com.tang.example1;

public class UserThread extends Thread{
	private final Gate gate;
	private final String myaddress;
	private final String myname;
	
	public UserThread(Gate gate, String myname, String myaddress){
		this.gate = gate;
		this.myaddress = myaddress;
		this.myname = myname;
		
	}
	
	@Override
	public void run() {
		super.run();
		System.out.println(myname + "begin");
		while(true){
			gate.pass(myname, myaddress);
		}
	}
}