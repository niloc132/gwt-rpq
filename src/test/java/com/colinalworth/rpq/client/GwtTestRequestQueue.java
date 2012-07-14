package com.colinalworth.rpq.client;

import com.colinalworth.rpq.client.RequestQueue.Service;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GwtTestRequestQueue extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.rpq.RPQ";
	}
	

	public static class TrivialService {
		
	}
	public interface TrivialServiceAsync {
		
	}
	public interface TrivialRequestQueue extends RequestQueue {
		@Service(TrivialService.class)
		TrivialServiceAsync sample();
	}
	
	public void testTrivialService() {
		TrivialRequestQueue rq = GWT.create(TrivialRequestQueue.class);
		rq.sample();
	}
	
	public static class SampleService {
		public String trim(String str) {
			return str.trim();
		}
	}
	public interface SampleServiceAsync {
		void trim(String a, AsyncCallback<String> b);
	}
	public interface SampleRequestQueue extends RequestQueue {
		@Service(SampleService.class)
		SampleServiceAsync sample();
	}
	
	public void testSimpleMethodInvocation() {
		SampleRequestQueue queue = GWT.create(SampleRequestQueue.class);
		queue.sample().trim("asdf", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				fail();
			}
			public void onSuccess(String result) {
				fail();
			}
		});
	}
	
//	public void testFireNoCalls() {
//		
//	}
	
//	public void testFireCalls() {
//		
//	}

}
