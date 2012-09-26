package com.colinalworth.rpq.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

public class GwtTestGenericsUsage extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.rpq.RPQTest";
	}
	public static class StringWrapper implements IsSerializable {
		public String string;
		public StringWrapper() {
		}
		public StringWrapper(String string) {
			this.string = string;
		}
	}

	//start simple, just List<StringWrapper> //not string, since we can probably already send string
	public static class StringJoinService {
		public String join(ArrayList<StringWrapper> list) {
			StringBuilder sb = new StringBuilder();
			for (StringWrapper s : list) {
				sb.append(s.string);
			}
			return sb.toString();
		}
	}
	public interface StringJoinServiceAsync {
		void join(List<StringWrapper> list, AsyncCallback<String> callback);
	}
	public interface StringQueue extends RequestQueue {
		@Service(StringJoinService.class)
		StringJoinServiceAsync join();
	}
	public void testSendListStringBuilder() {
		StringQueue q = GWT.create(StringQueue.class);
		ArrayList<StringWrapper> list = new ArrayList<StringWrapper>();
		list.add(new StringWrapper("hello"));
		q.join().join(list, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				assertEquals("hello", result);
				finishTest();
			}
			@Override
			public void onFailure(Throwable caught) {
				fail(caught.getMessage());
			}
		});
		q.fire();
		delayTestFinish(1000);
	}
	
	//then try a generic service, make concrete in the queue
	
	//next a generic service, kept generic in the queue decl
}
