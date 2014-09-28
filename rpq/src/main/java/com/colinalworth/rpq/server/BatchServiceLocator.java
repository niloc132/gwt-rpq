package com.colinalworth.rpq.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.colinalworth.rpq.shared.impl.BatchRequest;

public class BatchServiceLocator {
	private static final Map<String, Class<?>> TYPE_NAMES;

	static {
		TYPE_NAMES = new HashMap<String, Class<?>>();
		TYPE_NAMES.put("Z", boolean.class);
		TYPE_NAMES.put("B", byte.class);
		TYPE_NAMES.put("C", char.class);
		TYPE_NAMES.put("D", double.class);
		TYPE_NAMES.put("F", float.class);
		TYPE_NAMES.put("I", int.class);
		TYPE_NAMES.put("J", long.class);
		TYPE_NAMES.put("S", short.class);
	}

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
			try {
				types[i] = getClassFromSerializedName(req.getTypes()[i], Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Param " + i + " is of an unknown type: " + req.getTypes()[i]);
			}
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

	private static Class<?> getClassFromSerializedName(String serializedName, ClassLoader classLoader)
			throws ClassNotFoundException {
		Class<?> value = TYPE_NAMES.get(serializedName);
		if (value != null) {
			return value;
		}

		return Class.forName(serializedName, false, classLoader);
	}
}