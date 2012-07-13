package com.colinalworth.rpq.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.colinalworth.rpq.shared.impl.ServiceQueueBase;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

//TODO lie to RPC and tell it that we implement any interface
public class BatchServiceServlet extends RemoteServiceServlet implements ServiceQueueBase {
	
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

	private Object invoke(BatchRequest req) throws Exception, InvocationTargetException {
		// really quick impl, we want some kind of locator for this stuff
		Class<?> clazz = getServiceType(req);
		
		Method m = getServiceMethod(req, clazz);
		
		Object serviceInstance = getServiceInstance(clazz);
		
		return m.invoke(serviceInstance, req.getParams());
	}


	private Class<?> getServiceType(BatchRequest req) throws ClassNotFoundException {
		return Class.forName(req.getService());
	}

	private Method getServiceMethod(BatchRequest req, Class<?> serviceType) throws NoSuchMethodException, SecurityException {
		Class<?>[] types = new Class<?>[req.getParams().length];
		for (int i = 0; i < types.length; i++) {
			types[i] = req.getParams()[i].getClass();
		}
		return serviceType.getMethod(req.getMethod(), types);
	}

	private Object getServiceInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}
}
