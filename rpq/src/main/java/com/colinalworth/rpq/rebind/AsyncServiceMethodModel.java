package com.colinalworth.rpq.rebind;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;

public class AsyncServiceMethodModel implements Serializable {
	private static final long serialVersionUID = 1L;

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
		public Builder setReturnTypeName(String returnTypeName) {
			model.returnTypeName = returnTypeName;
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
	private String returnTypeName;
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
	public String getReturnTypeName() {
		return returnTypeName;
	}
	public Set<JClassType> getThrowables() {
		return throwables;
	}
}
