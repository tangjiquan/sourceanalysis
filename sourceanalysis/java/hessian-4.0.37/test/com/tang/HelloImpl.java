package com.tang;

public class HelloImpl implements Hello{
	private String helloStr = "hello world";
	
	@Override
	public String sayHello() {
		return helloStr;
	}
}
