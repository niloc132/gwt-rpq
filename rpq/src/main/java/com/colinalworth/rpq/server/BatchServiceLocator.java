package com.colinalworth.rpq.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.colinalworth.rpq.shared.impl.BatchRequest;

public class BatchServiceLocator {
	public Class<?> getServiceType(BatchRequest req) {
		try {
			return Class.forName(req.getService());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to find service " + req.getService());
		}
	}

	public Method getServiceMethod(BatchRequest req, Class<?> serviceType) {
		Class<?>[] types = new Class<?>[req.getParams().length];
		for (int i = 0; i < types.length; i++) {
			types[i] = req.getParams()[i].getClass();
		}

		try {
			return serviceType.getMethod(req.getMethod(), types);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to find method " + req.getMethod());
		}
	}

	public Object getServiceInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to instantiate " + clazz);
		}
	}

	public Object invoke(BatchRequest req, Method m, Object serviceInstance) throws InvocationTargetException {
		try {
			return m.invoke(serviceInstance, req.getParams());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to invoke " + m.getName() + " on " + serviceInstance, e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to invoke " + m.getName() + " on " + serviceInstance, e);
		}
	}
}