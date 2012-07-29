package com.colinalworth.rpqsample.demo.server;

import java.util.Date;

public class HelloService {
	public String sayHiTo(String name) {
		return "Hello there, " + name;
	}
	public Date getCurrentTime() {
		return new Date();
	}
	
	public void delay(Integer seconds) throws InterruptedException {
		Thread.sleep(seconds * 1000);
	}
}
