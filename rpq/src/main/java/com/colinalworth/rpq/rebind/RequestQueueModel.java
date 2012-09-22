package com.colinalworth.rpq.rebind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RequestQueueModel implements Serializable {
	private static final long serialVersionUID = 1L;

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
		public Builder setRequestQueueInterfaceName(String requestQueueInterfaceName) {
			model.requestQueueInterfaceName = requestQueueInterfaceName;
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
	private String requestQueueInterfaceName;

	private RequestQueueModel() {

	}
	public String getPath() {
		return path;
	}
	public List<AsyncServiceModel> getServices() {
		return services;
	}
	public String getRequestQueueInterfaceName() {
		return requestQueueInterfaceName;
	}
}
