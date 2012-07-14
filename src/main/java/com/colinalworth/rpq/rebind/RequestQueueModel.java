package com.colinalworth.rpq.rebind;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.JClassType;

public class RequestQueueModel {
	public static class Builder {
		private RequestQueueModel model = new RequestQueueModel();
		public Builder setPath(String path) {
			model.path = path;
			return this;
		}
		public Builder setServices(List<AsyncServiceModel> services) {
			model.services = services;
			return this;
		}
		public Builder addService(AsyncServiceModel build) {
			model.services.add(build);
			return this;
		}
		public Builder setRequestQueueInterface(JClassType requestQueueInterface) {
			model.requestQueueInterface = requestQueueInterface;
			return this;
		}

		public RequestQueueModel build() {
			RequestQueueModel model = this.model;
			this.model = new RequestQueueModel();
			return model;
		}
	}

	private String path;
	private List<AsyncServiceModel> services = new ArrayList<AsyncServiceModel>();
	private JClassType requestQueueInterface;

	private RequestQueueModel() {

	}
	public String getPath() {
		return path;
	}
	public List<AsyncServiceModel> getServices() {
		return services;
	}
	public JClassType getRequestQueueInterface() {
		return requestQueueInterface;
	}
}
