package com.colinalworth.rpq.client;

import java.util.Date;

import com.colinalworth.rpq.client.AsyncService.Throws;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcTokenException;

public class GwtTestRequestQueue extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.rpq.RPQTest";
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
		public void doSomething() {
			throw new RuntimeException("something");
		}
	}
	public interface SampleServiceAsync {
		void trim(String a, AsyncCallback<String> b);

		//test other types, colliding methodnames, exceptions
		@Throws({RuntimeException.class})
		void doSomething(AsyncCallback<Date> param);

		//test unboxed params, colliding method names, missing callback
		void doSomething(int i);
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
		queue.sample().doSomething(new AsyncCallback<Date>() {
			public void onSuccess(Date result) {
				fail();
			}
			public void onFailure(Throwable caught) {
				fail();
			}
		});
		queue.sample().doSomething(2);
	}

	public void testFireNoCalls() {
		//nothing should happen
		SampleRequestQueue queue = GWT.create(SampleRequestQueue.class);
		queue.fire();
	}

	public void testFireCalls() {
		SampleRequestQueue queue = GWT.create(SampleRequestQueue.class);
		queue.sample().trim("  asdf  ", new AsyncCallback<String>() {
			public void onSuccess(String result) {
				assertEquals("asdf", result);
				finishTest();
			}
			public void onFailure(Throwable caught) {
				GWT.log("fail", caught);
				fail(caught.getMessage());
			}
		});
		queue.fire();
		delayTestFinish(1000);
	}

	public void testFireAndCheckException() {
		SampleRequestQueue queue = GWT.create(SampleRequestQueue.class);
		queue.sample().doSomething(new AsyncCallback<Date>() {
			public void onSuccess(Date result) {
				fail();
			}
			public void onFailure(Throwable caught) {
				assertNotNull(caught);
				assertTrue(caught instanceof RuntimeException);
				assertEquals("something", caught.getMessage());
				finishTest();
			}
		});
		queue.fire();
		delayTestFinish(1000);
	}

}
