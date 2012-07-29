package com.colinalworth.rpqsample.defer.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SampleServiceAsync {
	void trimString(String str, AsyncCallback<String> callback);
	
	void addOne(Integer num, AsyncCallback<Integer> callback);
}
