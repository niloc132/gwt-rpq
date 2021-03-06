package com.colinalworth.rpq.client.impl;

import java.util.ArrayList;
import java.util.List;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.colinalworth.rpq.shared.impl.ServiceQueueBaseAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Base impl which will be subclassed by a generated class. 
 *
 */
//TODO consider making this just a static instance in the RequestQueue impl to avoid colliding with service methods names
public abstract class AbstractRequestQueueImpl implements RequestQueue {
	private List<BatchRequest> requests = new ArrayList<BatchRequest>();
	private List<AsyncCallback<Object>> callbacks = new ArrayList<AsyncCallback<Object>>();

	private String address = GWT.getModuleBaseURL() + "rpq";

	public void setServiceEntryPoint(String address) {
		this.address = address;
	}

	public void fire() {
		if (requests.size() == 0) {
			return;
		}

		ServiceQueueBaseAsync service = getRealService();
		((ServiceDefTarget)service).setServiceEntryPoint(address);

		// Save a local copy to close over, and reset the old for the next round
		final List<BatchRequest> activeRequests = new ArrayList<BatchRequest>(requests);
		final List<AsyncCallback<Object>> activeCallbacks = new ArrayList<AsyncCallback<Object>>(callbacks);
		assert activeCallbacks.size() == activeRequests.size();
		reset();

		// This should only reference the final vars above
		// TODO consider a static class for this to avoid accidental references
		service.batchedRequest(activeRequests, new AsyncCallback<List<BatchResponse>>() {
			public void onSuccess(List<BatchResponse> result) {
				int length = result.size();
				assert length == activeCallbacks.size();
				for (int i = 0; i < length; i++) {
					BatchResponse res = result.get(i);
					AsyncCallback<Object> callback = activeCallbacks.get(i);
					if (callback == null) {
						continue;
					}
					// if exception isn't null, there was an error
					// can't make this same check on the response, since it could be null or Void
					if (res.getCaught() != null) {
						callback.onFailure(res.getCaught());
					} else {
						callback.onSuccess(res.getResponse());
					}
				}
			}
			public void onFailure(Throwable caught) {//only gets called if the whole thing blew up
				int length = activeCallbacks.size();
				for (int i = 0; i < length; i++) {
					AsyncCallback<?> callback = activeCallbacks.get(i);
					if (callback != null) {
						activeCallbacks.get(i).onFailure(caught);
					}
				}
			}
		});
	}

	/**
	 * Adds a call to the queue. To be called by each generated method instead of an ajax call
	 */
	@SuppressWarnings("unchecked")
	protected void addRequest(String service, String method, String[] types, AsyncCallback<?> callback, Object... params) {
		requests.add(new BatchRequest(service, method, types, params));
		callbacks.add((AsyncCallback<Object>) callback);
	}

	protected void reset() {
		requests.clear();
		callbacks.clear();
	}

	/**
	 * Generated async interface that can handle serialization of all types expected to go
	 * over the wire.
	 */
	protected abstract ServiceQueueBaseAsync getRealService();

}
