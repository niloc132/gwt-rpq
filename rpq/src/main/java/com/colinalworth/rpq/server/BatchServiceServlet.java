package com.colinalworth.rpq.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.colinalworth.rpq.shared.impl.ServiceQueueBase;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.DequeMap;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

public class BatchServiceServlet extends RemoteServiceServlet implements ServiceQueueBase {

	private final BatchServiceLocator locator;

	public BatchServiceServlet() {
		this(new BatchServiceLocator());
	}

	public BatchServiceServlet(BatchServiceLocator batchServiceLocator) {
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


	//RemoteServiceServlet override to allow this servlet to handle all calls
	public String processCall(String payload) throws SerializationException {
		// First, check for possible XSRF situation
		checkPermutationStrongName();

		try {
			// Hijack decoding to skip the instanceof check it makes from the client
			// interface to this servlet

			// passing null is almost enough, but we still get caught up trying to use the
			// generated rpc interface, which isn't on the classpath
			RPCRequest rpcRequest = BatchServiceServlet.decodeRequest(payload, null, this);
			onAfterRequestDeserialized(rpcRequest);

			return RPC.invokeAndEncodeResponse(this, rpcRequest.getMethod(),
					rpcRequest.getParameters(), rpcRequest.getSerializationPolicy(),
					rpcRequest.getFlags());
		} catch (IncompatibleRemoteServiceException ex) {
			log(
					"An IncompatibleRemoteServiceException was thrown while processing this call.",
					ex);
			return RPC.encodeResponseForFailure(null, ex);
		} catch (RpcTokenException tokenException) {
			log("An RpcTokenException was thrown while processing this call.",
					tokenException);
			return RPC.encodeResponseForFailure(null, tokenException);
		}
	}


	// RPC overrides to allow the servlet to respond to any calls
	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS =
			new HashMap<Class<?>, Class<?>>();

	/**
	 * Static map of classes to sets of interfaces (e.g. classes). Optimizes
	 * lookup of interfaces for security.
	 */
	private static Map<Class<?>, Set<String>> serviceToImplementedInterfacesMap;

	private static final HashMap<String, Class<?>> TYPE_NAMES;

	static {
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Boolean.class, Boolean.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Byte.class, Byte.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Character.class, Character.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Double.class, Double.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Float.class, Float.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Integer.class, Integer.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Long.class, Long.TYPE);
		PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Short.class, Short.TYPE);

		TYPE_NAMES = new HashMap<String, Class<?>>();
		TYPE_NAMES.put("Z", boolean.class);
		TYPE_NAMES.put("B", byte.class);
		TYPE_NAMES.put("C", char.class);
		TYPE_NAMES.put("D", double.class);
		TYPE_NAMES.put("F", float.class);
		TYPE_NAMES.put("I", int.class);
		TYPE_NAMES.put("J", long.class);
		TYPE_NAMES.put("S", short.class);

		serviceToImplementedInterfacesMap = new HashMap<Class<?>, Set<String>>();
	}

	public static RPCRequest decodeRequest(String encodedRequest, Class<?> type,
			SerializationPolicyProvider serializationPolicyProvider) {
		if (encodedRequest == null) {
			throw new NullPointerException("encodedRequest cannot be null");
		}

		if (encodedRequest.length() == 0) {
			throw new IllegalArgumentException("encodedRequest cannot be empty");
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try {
			ServerSerializationStreamReader streamReader =
					new ServerSerializationStreamReader(classLoader, serializationPolicyProvider);
			streamReader.prepareToRead(encodedRequest);

			RpcToken rpcToken = null;
			if (streamReader.hasFlags(AbstractSerializationStream.FLAG_RPC_TOKEN_INCLUDED)) {
				// Read the RPC token
				rpcToken = (RpcToken) streamReader.deserializeValue(RpcToken.class);
			}

			// Read the name of the RemoteService interface
			String serviceIntfName = maybeDeobfuscate(streamReader, streamReader.readString());

			if (type != null) {
				if (!implementsInterface(type, serviceIntfName)) {
					// The service does not implement the requested interface
					throw new IncompatibleRemoteServiceException("Blocked attempt to access interface '"
							+ serviceIntfName + "', which is not implemented by '" + printTypeName(type)
							+ "'; this is either misconfiguration or a hack attempt");
				}
			}

			SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
			final Class<?> serviceIntf = ServiceQueueBase.class;

			String serviceMethodName = streamReader.readString();

			int paramCount = streamReader.readInt();
			if (paramCount > streamReader.getNumberOfTokens()) {
				throw new IncompatibleRemoteServiceException("Invalid number of parameters");
			}
			Class<?>[] parameterTypes = new Class[paramCount];

			for (int i = 0; i < parameterTypes.length; i++) {
				String paramClassName = maybeDeobfuscate(streamReader, streamReader.readString());

				try {
					parameterTypes[i] = getClassFromSerializedName(paramClassName, classLoader);
				} catch (ClassNotFoundException e) {
					throw new IncompatibleRemoteServiceException("Parameter " + i
							+ " of is of an unknown type '" + paramClassName + "'", e);
				}
			}

			try {
				Method method = serviceIntf.getMethod(serviceMethodName, parameterTypes);

				// The parameter types we have are the non-parameterized versions in the
				// RPC stream. For stronger message verification, get the parameterized
				// types from the method declaration.
				Type[] methodParameterTypes = method.getGenericParameterTypes();
				DequeMap<TypeVariable<?>, Type> resolvedTypes = new DequeMap<TypeVariable<?>, Type>();

				TypeVariable<Method>[] methodTypes = method.getTypeParameters();
				for (TypeVariable<Method> methodType : methodTypes) {
					SerializabilityUtil.resolveTypes(methodType, resolvedTypes);
				}

				Object[] parameterValues = new Object[parameterTypes.length];
				for (int i = 0; i < parameterValues.length; i++) {
					parameterValues[i] = streamReader.deserializeValue(parameterTypes[i],
							methodParameterTypes[i], resolvedTypes);
				}

				return new RPCRequest(method, parameterValues, rpcToken, serializationPolicy, streamReader
						.getFlags());
			} catch (NoSuchMethodException e) {
				throw new IncompatibleRemoteServiceException(formatMethodNotFoundErrorMessage(serviceIntf,
						serviceMethodName, parameterTypes));
			}
		} catch (SerializationException ex) {
			throw new IncompatibleRemoteServiceException(ex.getMessage(), ex);
		}
	}

	private static boolean implementsInterface(Class<?> service, String intfName) {
		synchronized (serviceToImplementedInterfacesMap) {
			// See if it's cached.
			//
			Set<String> interfaceSet = serviceToImplementedInterfacesMap.get(service);
			if (interfaceSet != null) {
				if (interfaceSet.contains(intfName)) {
					return true;
				}
			} else {
				interfaceSet = new HashSet<String>();
				serviceToImplementedInterfacesMap.put(service, interfaceSet);
			}

			if (!service.isInterface()) {
				while ((service != null) && !RemoteServiceServlet.class.equals(service)) {
					Class<?>[] intfs = service.getInterfaces();
					for (Class<?> intf : intfs) {
						if (implementsInterfaceRecursive(intf, intfName)) {
							interfaceSet.add(intfName);
							return true;
						}
					}

					// did not find the interface in this class so we look in the
					// superclass
					//
					service = service.getSuperclass();
				}
			} else {
				if (implementsInterfaceRecursive(service, intfName)) {
					interfaceSet.add(intfName);
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Only called from implementsInterface().
	 */
	private static boolean implementsInterfaceRecursive(Class<?> clazz, String intfName) {
		assert (clazz.isInterface());

		if (clazz.getName().equals(intfName)) {
			return true;
		}

		// search implemented interfaces
		Class<?>[] intfs = clazz.getInterfaces();
		for (Class<?> intf : intfs) {
			if (implementsInterfaceRecursive(intf, intfName)) {
				return true;
			}
		}

		return false;
	}

	private static String maybeDeobfuscate(ServerSerializationStreamReader streamReader, String name)
			throws SerializationException {
		int index;
		if (streamReader.hasFlags(AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES)) {
			SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
			if (!(serializationPolicy instanceof TypeNameObfuscator)) {
				throw new IncompatibleRemoteServiceException(
						"RPC request was encoded with obfuscated type names, "
								+ "but the SerializationPolicy in use does not implement "
								+ TypeNameObfuscator.class.getName());
			}

			String maybe = ((TypeNameObfuscator) serializationPolicy).getClassNameForTypeId(name);
			if (maybe != null) {
				return maybe;
			}
		} else if ((index = name.indexOf('/')) != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	private static String printTypeName(Class<?> type) {
		// Primitives
		//
		if (type.equals(Integer.TYPE)) {
			return "int";
		} else if (type.equals(Long.TYPE)) {
			return "long";
		} else if (type.equals(Short.TYPE)) {
			return "short";
		} else if (type.equals(Byte.TYPE)) {
			return "byte";
		} else if (type.equals(Character.TYPE)) {
			return "char";
		} else if (type.equals(Boolean.TYPE)) {
			return "boolean";
		} else if (type.equals(Float.TYPE)) {
			return "float";
		} else if (type.equals(Double.TYPE)) {
			return "double";
		}

		// Arrays
		//
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			return printTypeName(componentType) + "[]";
		}

		// Everything else
		//
		return type.getName().replace('$', '.');
	}
	private static Class<?> getClassFromSerializedName(String serializedName, ClassLoader classLoader)
			throws ClassNotFoundException {
		Class<?> value = TYPE_NAMES.get(serializedName);
		if (value != null) {
			return value;
		}

		return Class.forName(serializedName, false, classLoader);
	}

	private static String formatMethodNotFoundErrorMessage(Class<?> serviceIntf,
			String serviceMethodName, Class<?>[] parameterTypes) {
		StringBuffer sb = new StringBuffer();

		sb.append("Could not locate requested method '");
		sb.append(serviceMethodName);
		sb.append("(");
		for (int i = 0; i < parameterTypes.length; ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(printTypeName(parameterTypes[i]));
		}
		sb.append(")'");

		sb.append(" in interface '");
		sb.append(printTypeName(serviceIntf));
		sb.append("'");

		return sb.toString();
	}
}
