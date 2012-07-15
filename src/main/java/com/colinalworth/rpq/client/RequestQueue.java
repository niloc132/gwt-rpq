package com.colinalworth.rpq.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface RequestQueue {
	/**
	 * Fires all accumulated service calls. Allows multiple requests to be made at once.
	 * 
	 * Declared methods should have a return type that extends AsyncService (easier validation this
	 * way, and maybe we can add other service-level methods that way?)
	 * 
	 * Clever/stupid/novel ideas:
	 *  o  Call this in a deferred command, with a check that it hasn't yet been deferred this loop.
	 *  o  Call this in a runAsync call, moving all RPC code into the same split point
	 */
	void fire();
	
	/**
	 * Like {@link ServiceDefTarget#setServiceEntryPoint(String)}
	 * @param address
	 */
	void setServiceEntryPoint(String address);

	/**
	 * Wires a service interface to the server implementation
	 * @author colin
	 *
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Service {
		Class<?> value();
	}
}
