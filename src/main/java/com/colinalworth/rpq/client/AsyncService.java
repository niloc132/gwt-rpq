package com.colinalworth.rpq.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Not actually required yet, but should be the interface extended by the async service methods.
 * 
 * @author colin
 *
 */
public interface AsyncService {

	/**
	 * Declares what exceptions are allowed to be thrown by this method
	 * @author colin
	 *
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Throws {
		Class<? extends Throwable>[] value();
	}
}
