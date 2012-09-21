package com.colinalworth.rpq.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GwtTestPrimitiveTypes extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.rpq.RPQTest";
	}
	
	public static class PrimitiveService {
		public int add(int a, int b) {
			return a + b;
		}
		
		
	}

	public interface PrimitiveServiceAsync {
		void add(int a, int b, AsyncCallback<Integer> callback);
	}
	
	public interface PrimitiveRequestQueue extends RequestQueue {
		@Service(PrimitiveService.class)
		PrimitiveServiceAsync service();
	}
	
	public void testAllPrimitives() {
		PrimitiveRequestQueue q = GWT.create(PrimitiveRequestQueue.class);
		PrimitiveServiceAsync service = q.service();
		service.add(1, 2, new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				assertEquals(3, result.intValue());
				finishTest();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				fail(caught.getMessage());
			}
		});
		q.fire();
		delayTestFinish(1000);
	}

}
