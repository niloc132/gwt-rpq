package com.colinalworth.rpq.client;

import com.colinalworth.rpq.client.RequestQueue.Service;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestRequestQueue extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.rpq.RPQ";
	}
	

	public static class SampleService {
		
	}
	public interface SampleServiceAsync {
		
	}
	public interface SampleRequestQueue extends RequestQueue {
		@Service(SampleService.class)
		SampleServiceAsync sample();
	}
	
	public void testTrivialService() {
		SampleRequestQueue rq = GWT.create(SampleRequestQueue.class);
		rq.sample();
	}

}
