package com.tang;

import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;

public class TestHessian {
	public static void main(String[] args) throws MalformedURLException {
		String url = "http://localhost:8080/hessian-4.0.37/hello.xsp";
		HessianProxyFactory factory = new HessianProxyFactory();
		Hello hello = (Hello)factory.create(Hello.class, url);
		System.out.println(hello.sayHello());
	}
}
