package com.colinalworth.rpqsample.defer.shared;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpqsample.defer.server.SampleService;

public interface SampleQueue extends RequestQueue {
	@Service(SampleService.class)
	SampleServiceAsync service();
}
