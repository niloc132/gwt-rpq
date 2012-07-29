package com.colinalworth.rpq.shared.impl;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BatchRequest implements IsSerializable {
	private transient String service;
	private transient String method;
	
	private transient Object[] params;

	public BatchRequest() {
		// rpc serializable
	}
	
	public BatchRequest(String service, String method, Object[] params) {
		setService(service);
		setMethod(method);
		setParams(params);
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}
	
}