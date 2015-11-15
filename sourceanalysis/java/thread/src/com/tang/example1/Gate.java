package com.tang.example1;

public class Gate {
	private int count = 0;
	private String name = "Nobody";
	private String address = "Nowhere";
	
	public synchronized void pass(String name, String address){
		this.name = name;
		this.address = address;
		this.count++;
		check();
	}
	
	private void check(){
		if(name.charAt(0) != address.charAt(0)){
			System.out.println("******"+toString());
		}
	}
	
	public  String toString(){
		return "no." + count + ": " + name + ", " + address;
	}
	
	
	
}
