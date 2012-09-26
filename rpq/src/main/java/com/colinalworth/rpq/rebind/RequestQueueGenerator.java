package com.colinalworth.rpq.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.colinalworth.rpq.client.AsyncService.Throws;
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
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class RequestQueueGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();

		//JClassType requestQueue = oracle.findType(RequestQueue.class.getName());
		JClassType toGenerate = oracle.findType(typeName);

		if (toGenerate == null) {
			logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
			throw new UnableToCompleteException();
		}

		String packageName = toGenerate.getPackage().getName();
		String simpleSourceName = toGenerate.getName().replace('.', '_') + "_Impl";
		PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
		if (pw == null) {
			return packageName + "." + simpleSourceName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);
		factory.setSuperclass(AbstractRequestQueueImpl.class.getName());
		factory.addImplementedInterface(typeName);
		SourceWriter sw = factory.createSourceWriter(context, pw);

		// Collect async services we need to provide access to, and the rpc calls they'll make
		RequestQueueModel model = collectModel(logger.branch(Type.DEBUG, "Collecting service info"), context, toGenerate);


		// Build a pair of RPC interfaces for serialization
		String realRpcInterfaceName = buildRpcInterfaces(logger.branch(Type.DEBUG, "Writing RPC interfaces"), context, model);


		// Build the getRealService() method
		String serviceQueueAsync = ServiceQueueBaseAsync.class.getName();
		sw.println("public %1$s getRealService() {", serviceQueueAsync);
		sw.indentln("return %3$s.<%1$s>create(%2$s.class);", serviceQueueAsync, realRpcInterfaceName, GWT.class.getName());
		sw.println("}");


		// Build the methods (and maybe types?) that call addRequest()
		for (AsyncServiceModel service : model.getServices()) {
			sw.println("public %1$s %2$s() {", service.getAsyncServiceInterfaceName(), service.getDeclaredMethodName());
			sw.indent();
			
			sw.println("return new %1$s() {", service.getAsyncServiceInterfaceName());
			sw.indent();
			
			for (AsyncServiceMethodModel method : service.getMethods()) {
				sw.println("public void %1$s(", method.getMethodName());
				StringBuilder argList = new StringBuilder();
				for (int i = 0; i < method.getArgTypes().size(); i++) {
					if (i != 0) {
						sw.print(", ");
						argList.append(", ");
					}
					JType arg = method.getArgTypes().get(i);

					sw.print("%1$s arg%2$d", arg.getParameterizedQualifiedSourceName(), i);
					argList.append("arg").append(i);
				}
				if (method.hasCallback()) {
					if (method.getArgTypes().size() != 0) {
						sw.print(", ");
					}
					sw.print("%1$s<%2$s> callback", AsyncCallback.class.getName(), method.getReturnTypeName());
				}
				sw.println(") {");
				sw.indent();
				
				sw.println("addRequest(\"%1$s\", \"%2$s\",", escape(service.getServiceName()), escape(method.getMethodName()));
				if (method.hasCallback()) {
					sw.indentln("callback,");
				} else {
					sw.indentln("null,");
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
		
		TypeOracle typeOracle = context.getTypeOracle();
		JClassType requestQueue = typeOracle.findType(RequestQueue.class.getName());
		JClassType asyncCallback = typeOracle.findType(AsyncCallback.class.getName());
		JClassType voidType = typeOracle.findType(Void.class.getName());
		rqBuilder.setRequestQueueInterfaceName(toGenerate.getParameterizedQualifiedSourceName());

		AsyncServiceModel.Builder serviceBuilder = new AsyncServiceModel.Builder();
		for (JMethod m : toGenerate.getMethods()) {
			TreeLogger serviceLogger = logger.branch(Type.DEBUG, "Reading async service " + m.getReadableDeclaration());
			
			// Skip those defined at RequestQueue
			if (m.getEnclosingType().equals(requestQueue)) {
				continue;
			}
			JClassType returnType = m.getReturnType().isClassOrInterface();
			if (returnType == null) {
				serviceLogger.log(Type.ERROR, "Unexpected method return type " + returnType);
				throw new UnableToCompleteException();
			}

			serviceBuilder.setAsyncServiceInterfaceName(returnType.getParameterizedQualifiedSourceName());
			serviceBuilder.setDeclaredMethodName(m.getName());

			Service serviceAnnotation = m.getAnnotation(Service.class);
			if (serviceAnnotation == null) {
				serviceLogger.log(Type.ERROR, "Missing @Service annotation");
				throw new UnableToCompleteException();
			}
			serviceBuilder.setService(serviceAnnotation.value().getName());
			
			
			AsyncServiceMethodModel.Builder methodBuilder = new AsyncServiceMethodModel.Builder();
			for (JMethod asyncMethod : m.getReturnType().isClassOrInterface().getMethods()) {
				TreeLogger methodLogger = serviceLogger.branch(Type.DEBUG, "Reading service method " + asyncMethod.getReadableDeclaration());

				List<JType> types = new ArrayList<JType>();
				methodBuilder.setReturnTypeName(voidType.getParameterizedQualifiedSourceName());
				boolean asyncFound = false;
				for (JType param : asyncMethod.getParameterTypes()) {
					if (asyncFound) {
						methodLogger.log(Type.WARN, "Already passed an AsyncCallback param - is that what you meant?");
					}
					if (param.isClassOrInterface() != null && param.isClassOrInterface().isAssignableTo(asyncCallback)) {
						JClassType boxedReturnType = ModelUtils.findParameterizationOf(asyncCallback, param.isClassOrInterface())[0];
						methodBuilder
							.setHasCallback(true)
							.setReturnTypeName(boxedReturnType.getParameterizedQualifiedSourceName());
						asyncFound = true;
						continue;//should be last, check for this...
					}
					types.add(param);
				}
				Set<JClassType> throwables = new HashSet<JClassType>();
				Throws t = asyncMethod.getAnnotation(Throws.class);
				if (t != null) {
					for (Class<? extends Throwable> throwable : t.value()) {
						throwables.add(typeOracle.findType(throwable.getName()));
					}
				}
				
				methodBuilder
					.setMethodName(asyncMethod.getName())
					.setArgTypes(types)
					.setThrowables(throwables);
				
				serviceBuilder.addMethod(methodBuilder.build());
			}
			rqBuilder.addService(serviceBuilder.build());
		}
		
		return rqBuilder.build();
	}

	private String buildRpcInterfaces(TreeLogger logger, GeneratorContext context, RequestQueueModel model) {
		String rqType = model.getRequestQueueInterfaceName();
		int lastDot = rqType.lastIndexOf('.');
		String packageName = rqType.substring(0, lastDot - 1);
		String serviceSourceName = rqType.substring(lastDot).replace('.', '_') + "_ImplRPC";
		String asyncSourceName = serviceSourceName + "Async";
		PrintWriter pw = context.tryCreate(logger, packageName, asyncSourceName);
		if (pw == null) {
			return packageName + "." + serviceSourceName;
		}

		// Create async class
		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, asyncSourceName);
		factory.addImplementedInterface(ServiceQueueBaseAsync.class.getName());
		factory.makeInterface();
		SourceWriter asyncSw = factory.createSourceWriter(context, pw);

		// Create service class
		pw = context.tryCreate(logger, packageName, serviceSourceName);
		assert pw != null;
		factory = new ClassSourceFileComposerFactory(packageName, serviceSourceName);
		factory.addImplementedInterface(ServiceQueueBase.class.getName());
		factory.makeInterface();
		SourceWriter serviceSw = factory.createSourceWriter(context, pw);

		//write out service and async
		int i = 0;
		for (AsyncServiceModel service : model.getServices()) {
			for (AsyncServiceMethodModel method : service.getMethods()) {
				String methodName = "a" + ++i;
				asyncSw.println("void %1$s(", methodName);
				serviceSw.println("%2$s %1$s(", methodName, method.getReturnTypeName());
				asyncSw.indent();
				serviceSw.indent();
				
				boolean firstArgument = true;
				int argIndex = 0;
				for (JType arg : method.getArgTypes()) {
					if (!firstArgument) {
						asyncSw.println(",");
						serviceSw.println(",");
					}
					firstArgument = false;
					
					if (arg.isPrimitive() != null) {
						JPrimitiveType t = arg.isPrimitive();
						asyncSw.println("%2$s arg%1$d", argIndex, t.getQualifiedBoxedSourceName());
						serviceSw.println("%2$s arg%1$d", argIndex, t.getQualifiedBoxedSourceName());
					} else {
						asyncSw.println("%2$s arg%1$d", argIndex, arg.getParameterizedQualifiedSourceName());
						serviceSw.println("%2$s arg%1$d", argIndex, arg.getParameterizedQualifiedSourceName());
					}
					
					argIndex++;
				}
				if (!firstArgument) {
					asyncSw.print(", ");
				}
				//TODO return type boxing
				asyncSw.println("%1$s<%2$s>callback);", AsyncCallback.class.getName(), method.getReturnTypeName());
				serviceSw.print(")");
				if (!method.getThrowables().isEmpty()) {
					serviceSw.print(" throws ");
					boolean firstThrowable = true;
					for (JClassType throwable : method.getThrowables()) {
						if (!firstThrowable) {
							serviceSw.print(", ");
						}
						firstThrowable = false;
						serviceSw.print(throwable.getQualifiedSourceName());
					}
				}
				serviceSw.println(";");
				asyncSw.outdent();
				serviceSw.outdent();
			}
		}
		
		asyncSw.commit(logger);
		serviceSw.commit(logger);
		
		return factory.getCreatedClassName();
	}

}
