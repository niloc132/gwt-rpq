package com.colinalworth.rpq.shared.impl;

public class BatchResponse {
	private Object response;
	private Throwable caught;
	
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
