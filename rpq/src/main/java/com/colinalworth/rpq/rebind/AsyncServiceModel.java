package com.colinalworth.rpq.rebind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AsyncServiceModel implements Serializable {
	private static final long serialVersionUID = 1L;

	public static class Builder {
		private AsyncServiceModel model = new AsyncServiceModel();
		public Builder setService(String service) {
			model.service = service;
			return this;
		}
		public Builder setMethods(List<AsyncServiceMethodModel> methods) {
			model.methods = methods;
			return this;
		}
		public Builder addMethod(AsyncServiceMethodModel method) {
			model.methods.add(method);
			return this;
		}
		public Builder setAsyncServiceInterfaceName(String asyncServiceInterfaceName) {
			model.asyncServiceInterfaceName = asyncServiceInterfaceName;
			return this;
		}
		public Builder setDeclaredMethodName(String declaredMethodName) {
			model.declaredMethodName = declaredMethodName;
			return this;
		}

		public AsyncServiceModel build() {
			AsyncServiceModel model = this.model;
			this.model = new AsyncServiceModel();
			return model;
		}
	}

	private String service;
	private List<AsyncServiceMethodModel> methods = new ArrayList<AsyncServiceMethodModel>();
	private String asyncServiceInterfaceName;
	private String declaredMethodName;

	private AsyncServiceModel() {

	}
	public String getServiceName() {
		return service;
	}
	public List<AsyncServiceMethodModel> getMethods() {
		return methods;
	}
	public String getAsyncServiceInterfaceName() {
		return asyncServiceInterfaceName;
	}
	public String getDeclaredMethodName() {
		return declaredMethodName;
	}
}
