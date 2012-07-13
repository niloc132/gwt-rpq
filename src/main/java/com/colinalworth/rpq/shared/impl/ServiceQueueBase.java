package com.colinalworth.rpq.shared.impl;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ServiceQueueBase extends RemoteService {
	List<BatchResponse> batchedRequest(List<BatchRequest> requests);
}
