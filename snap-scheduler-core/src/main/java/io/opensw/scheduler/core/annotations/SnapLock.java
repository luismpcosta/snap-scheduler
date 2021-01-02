package io.opensw.scheduler.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.METHOD, ElementType.ANNOTATION_TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface SnapLock {

	/**
	 * Key of lock, this key was unique to identify task
	 * 
	 * @return key
	 */
	String key() default "";

	/**
	 * Time in seconds that lock was active
	 * 
	 * @return defined time or default time (30 seconds)
	 */
	long time() default 30;

}
