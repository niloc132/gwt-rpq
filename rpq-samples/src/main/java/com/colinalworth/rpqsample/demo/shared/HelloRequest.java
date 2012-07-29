package com.colinalworth.rpqsample.demo.shared;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpqsample.demo.server.HelloService;

public interface HelloRequest extends RequestQueue {
	@Service(HelloService.class)
	HelloServiceAsync helloService();
}
