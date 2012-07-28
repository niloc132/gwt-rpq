package com.colinalworth.rpq.rebind;

import java.util.List;
import java.util.Set;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;

public class AsyncServiceMethodModel {
	public static class Builder {
		private AsyncServiceMethodModel model = new AsyncServiceMethodModel();

		public Builder setMethodName(String methodName) {
			model.methodName = methodName;
			return this;
		}
		public Builder setHasCallback(boolean hasCallback) {
			model.hasCallback = hasCallback;
			return this;
		}
		public Builder setArgTypes(List<JType> argTypes) {
			model.argTypes = argTypes;
			return this;
		}
		public Builder setReturnType(JClassType returnType) {
			model.returnType = returnType;
			return this;
		}
		public Builder setThrowables(Set<JClassType> throwables) {
			model.throwables = throwables;
			return this;
		}

		public AsyncServiceMethodModel build() {
			AsyncServiceMethodModel model = this.model;
			this.model = new AsyncServiceMethodModel();
			return model;
		}
	}

	private String methodName;
	private boolean hasCallback;
	private List<JType> argTypes;
	private JClassType returnType;
	private Set<JClassType> throwables;
	
	private AsyncServiceMethodModel() {

	}
	public String getMethodName() {
		return methodName;
	}
	public boolean hasCallback() {
		return hasCallback;
	}
	public List<JType> getArgTypes() {
		return argTypes;
	}
	public JClassType getReturnType() {
		return returnType;
	}
	public Set<JClassType> getThrowables() {
		return throwables;
	}
}
