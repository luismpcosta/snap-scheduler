package io.opensw.scheduler.core.exceptions;

/**
 * BeanDefinitionException was throw when a task do not have right definition
 * 
 * @author luis.costa
 *
 */
public class BeanDefinitionException extends Exception {

	private static final long serialVersionUID = 2049205894475318046L;

	/**
	 * BeanDefinitionException constructor
	 */
	public BeanDefinitionException() {
		super("Bean definition error occur.");
	}

	/**
	 * BeanDefinitionException constructor with custom message
	 * 
	 * @param message to instantiate exception
	 */
	public BeanDefinitionException( final String message ) {
		super( message );
	}

}
