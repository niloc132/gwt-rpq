package com.colinalworth.rpq.rebind;

import java.io.PrintWriter;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpq.client.RequestQueue.Service;
import com.colinalworth.rpq.client.impl.AbstractRequestQueueImpl;
import com.colinalworth.rpq.shared.impl.ServiceQueueBase;
import com.colinalworth.rpq.shared.impl.ServiceQueueBaseAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class RequestQueueGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {

		TypeOracle oracle = context.getTypeOracle();

		JClassType requestQueue = oracle.findType(RequestQueue.class.getName());
		JClassType toGenerate = oracle.findType(typeName);

		if (toGenerate == null) {
			logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
			throw new UnableToCompleteException();
		}

		String packageName = toGenerate.getPackage().getName();
		String simpleSourceName = toGenerate.getName().replace('.', '_') + "Impl";
		PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
		if (pw == null) {
			return packageName + "." + simpleSourceName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);
		factory.setSuperclass(AbstractRequestQueueImpl.class.getName());
		factory.addImplementedInterface(typeName);
		SourceWriter sw = factory.createSourceWriter(context, pw);

		// Collect async services we need to provide access to, and the rpc calls they'll make
		RequestQueueModel model = collectModel(logger, context, toGenerate);


		// Build a pair of RPC interfaces for serialization
		String realRpcInterfaceName = buildRpcInterfaces(logger, context, model);


		// Build the getRealService() method
		String serviceQueueAsync = ServiceQueueBaseAsync.class.getName();
		sw.println("public %1$s getRealService() {", serviceQueueAsync);
		sw.indentln("return %3$s.<%1$s>create(%2$s.class);", serviceQueueAsync, realRpcInterfaceName, GWT.class.getName());
		sw.println("}");


		// Build the methods (and maybe types?) that call addRequest()
		for (AsyncServiceModel service : model.getServices()) {
			sw.println("public %1$s %2$s() {", service.getAsyncServiceInterface().getParameterizedQualifiedSourceName(), service.getDeclaredMethodName());
			sw.indent();
			
			sw.println("return new %1$s() {", service.getAsyncServiceInterface().getParameterizedQualifiedSourceName());
			sw.indent();
			
			for (AsyncServiceMethodModel method : service.getMethods()) {
				sw.println("public void %1$s(", method.getMethodName());
				StringBuilder argList = new StringBuilder();
				for (int i = 0; i < method.getArgTypes().size(); i++) {
					if (i != 0) {
						sw.print(",");
						argList.append(",");
					}
					JClassType arg = method.getArgTypes().get(i);

					sw.print("%1$s arg%2$d", arg.getParameterizedQualifiedSourceName(), i);
					argList.append("arg").append(i);
				}
				if (method.hasCallback()) {
					sw.print("%2$s<%2$s> callback", AsyncCallback.class.getName(), method.getReturnType().getParameterizedQualifiedSourceName());
				}
				sw.println(") {");
				sw.indent();
				
				sw.println("return addRequest(\"%1$s\", \"%1$s\",", escape(service.getServiceName()), escape(method.getMethodName()));
				if (method.hasCallback()) {
					sw.indentln("callback");
				} else {
					sw.indentln("null");
				}
				sw.indentln("new Object[]{%1$s});", argList.toString());
				
				sw.outdent();
				sw.println("}");
			}
			
			sw.outdent();
			sw.println("};");
			
			sw.outdent();
			sw.println("}");
		}



		sw.commit(logger);

		return factory.getCreatedClassName();
	}

	private RequestQueueModel collectModel(TreeLogger logger, GeneratorContext context, JClassType toGenerate) throws UnableToCompleteException {
		RequestQueueModel.Builder rqBuilder = new RequestQueueModel.Builder();
		JClassType requestQueue = context.getTypeOracle().findType(RequestQueue.class.getName());
		rqBuilder.setRequestQueueInterface(requestQueue);
		
//		List<JMethod> asyncServices = new ArrayList<JMethod>();
//		List<JMethod> asyncCalls = new ArrayList<JMethod>();
		AsyncServiceModel.Builder serviceBuilder = new AsyncServiceModel.Builder();
		for (JMethod m : toGenerate.getMethods()) {
			// Skip those defined at RequestQueue
			if (m.getEnclosingType().equals(requestQueue)) {
				continue;
			}
			JClassType returnType = m.getReturnType().isClassOrInterface();
			if (returnType == null) {
				logger.log(Type.ERROR, "Unexpected method return type " + returnType);
				throw new UnableToCompleteException();
			}

			//TODO log errors on this next line, no help without this
			serviceBuilder.setAsyncServiceInterface(returnType);
			serviceBuilder.setDeclaredMethodName(m.getName());
			serviceBuilder.setService(m.getAnnotation(Service.class).value().getName());
			
			
//			asyncServices.add(m);
			AsyncServiceMethodModel.Builder methodBuilder = new AsyncServiceMethodModel.Builder();
			for (JMethod asyncMethod : m.getReturnType().isClassOrInterface().getMethods()) {
				methodBuilder.setMethodName(asyncMethod.getName());
//				asyncMethod.getParameters();
				methodBuilder.setReturnType(returnType);
				serviceBuilder.addMethod(methodBuilder.build());
//				asyncCalls.add(asyncMethod);
			}
			rqBuilder.addService(serviceBuilder.build());
		}
		
		return rqBuilder.build();
	}

	private String buildRpcInterfaces(TreeLogger logger, GeneratorContext context, RequestQueueModel model) {
		JClassType rqType = model.getRequestQueueInterface();
		String packageName = rqType.getPackage().getName();
		String serviceSourceName = rqType.getName().replace('.', '_') + "ImplRPC";
		String asyncSourceName = serviceSourceName + "Async";
		PrintWriter pw = context.tryCreate(logger, packageName, asyncSourceName);
		if (pw == null) {
			return packageName + "." + serviceSourceName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, asyncSourceName);
		factory.addImplementedInterface(ServiceQueueBaseAsync.class.getName());
		factory.makeInterface();
		SourceWriter sw = factory.createSourceWriter(context, pw);
		//write out service
		sw.commit(logger);
		
		pw = context.tryCreate(logger, packageName, serviceSourceName);
		assert pw != null;
		factory = new ClassSourceFileComposerFactory(packageName, serviceSourceName);
		factory.addImplementedInterface(ServiceQueueBase.class.getName());
		factory.makeInterface();
		sw = factory.createSourceWriter(context, pw);
		//write out async
		sw.commit(logger);
		
		return factory.getCreatedClassName();
	}

}
