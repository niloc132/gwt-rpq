package com.colinalworth.rpq.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.colinalworth.rpq.shared.impl.ServiceQueueBase;

public class BatchInvoker implements ServiceQueueBase {
	private final BatchServiceLocator locator;

	public BatchInvoker() {
		this(new BatchServiceLocator());
	}
	
	public BatchInvoker(BatchServiceLocator batchServiceLocator) {
		this.locator = batchServiceLocator;
	}

	public List<BatchResponse> batchedRequest(List<BatchRequest> requests) {
		List<BatchResponse> responses = new ArrayList<BatchResponse>();
		for (BatchRequest req : requests) {
			BatchResponse resp = new BatchResponse();
			responses.add(resp);
			try {
				resp.setResponse(invoke(req));
			} catch (InvocationTargetException ex) {
				// exceptions from within the service call

				// I'm a little concerned about sending undeclared runtime exceptions here and
				// poisoning all other responses
				resp.setCaught(ex.getTargetException());
			} catch (Exception ex) {
				// All other errors, wrap them, since they might not make it over the wire
				resp.setCaught(new Exception(ex.getMessage()));
			}
		}
		return responses;
	}

	private Object invoke(BatchRequest req) throws ReflectiveOperationException {
		Class<?> clazz = getServiceType(req);

		Method m = getServiceMethod(req, clazz);

		Object serviceInstance = getServiceInstance(clazz);

		return invoke(req, m, serviceInstance);
	}


	private Object invoke(BatchRequest req, Method m, Object serviceInstance) throws InvocationTargetException {
		return locator.invoke(req, m, serviceInstance);
	}


	private Class<?> getServiceType(BatchRequest req) {
		return locator.getServiceType(req);
	}

	private Method getServiceMethod(BatchRequest req, Class<?> serviceType) {
		return locator.getServiceMethod(req, serviceType);
	}

	private Object getServiceInstance(Class<?> clazz) {
		return locator.getServiceInstance(clazz);
	}

}
