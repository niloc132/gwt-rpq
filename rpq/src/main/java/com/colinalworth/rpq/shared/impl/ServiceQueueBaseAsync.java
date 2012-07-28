package com.colinalworth.rpq.shared.impl;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServiceQueueBaseAsync {
	void batchedRequest(List<BatchRequest> requests, AsyncCallback<List<BatchResponse>> callback);
}
