package com.colinalworth.rpq.rebind;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.colinalworth.rpq.client.RequestQueue;
import com.colinalworth.rpq.client.impl.AbstractRequestQueueImpl;
import com.colinalworth.rpq.shared.impl.ServiceQueueBaseAsync;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
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
		List<JMethod> asyncServices = new ArrayList<JMethod>();
		List<JMethod> asyncCalls = new ArrayList<JMethod>();
		for (JMethod m : toGenerate.getMethods()) {
			// Skip those defined at RequestQueue
			if (m.getEnclosingType().equals(requestQueue)) {
				continue;
			}

			asyncServices.add(m);
			for (JMethod asyncMethod : m.getReturnType().isClassOrInterface().getMethods()) {
				asyncCalls.add(asyncMethod);
			}
		}

		// Build a pair of RPC interfaces for serialization


		// Build the getRealService() method


		// Build the methods (and maybe types?) that call addRequest()



		sw.commit(logger);

		return factory.getCreatedClassName();
	}


}
