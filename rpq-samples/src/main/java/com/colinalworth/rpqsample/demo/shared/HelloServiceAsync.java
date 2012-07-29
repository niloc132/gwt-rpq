package com.colinalworth.rpqsample.demo.shared;

import java.util.Date;

import com.colinalworth.rpq.client.AsyncService;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HelloServiceAsync extends AsyncService {
	void sayHiTo(String name, AsyncCallback<String> callback);
	
	void getCurrentTime(AsyncCallback<Date> callback);
	
	void delay(Integer delay);
}
