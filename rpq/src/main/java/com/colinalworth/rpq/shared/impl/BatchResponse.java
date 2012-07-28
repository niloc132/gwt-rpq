package com.colinalworth.rpq.shared.impl;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BatchResponse implements IsSerializable {
	private transient Object response;
	private transient Throwable caught;
	
	public BatchResponse() {
		//rpc-able
	}
	public BatchResponse(Object response) {
		setResponse(response);
	}
	public BatchResponse(Throwable caught) {
		setCaught(caught);
	}
	public Object getResponse() {
		return response;
	}
	public void setResponse(Object response) {
		this.response = response;
	}
	public Throwable getCaught() {
		return caught;
	}
	public void setCaught(Throwable caught) {
		this.caught = caught;
	}
	
}
