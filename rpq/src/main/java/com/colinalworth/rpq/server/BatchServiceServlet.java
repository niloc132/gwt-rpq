package com.colinalworth.rpq.server;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class BatchServiceServlet extends RemoteServiceServlet {
	private Object delegate;
	public BatchServiceServlet() {
		this(new BatchServiceLocator());
	}
	public BatchServiceServlet(BatchServiceLocator batchServiceLocator) {
		this(new BatchInvoker(batchServiceLocator));
	}

	public BatchServiceServlet(BatchInvoker delegate) {
		super(delegate);
		this.delegate = delegate;
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
			RPCRequest rpcRequest = BatchInvoker.decodeRequest(payload, null, this);
			onAfterRequestDeserialized(rpcRequest);

			return RPC.invokeAndEncodeResponse(delegate, rpcRequest.getMethod(),
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
}
