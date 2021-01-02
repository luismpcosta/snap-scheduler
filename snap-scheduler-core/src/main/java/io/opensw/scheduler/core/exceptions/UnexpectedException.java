package io.opensw.scheduler.core.exceptions;

/**
 * UnexpectedException was throw when unexpected exception occur
 * 
 * @author luis.costa
 *
 */
public class UnexpectedException extends Exception {

	private static final long serialVersionUID = 5082758194261846551L;

	/**
	 * UnexpectedException constructor
	 */
	public UnexpectedException() {
		super("Inexpected exception error occur.");
	}

	/**
	 * UnexpectedException constructor with custom message
	 * 
	 * @param message to instantiate exception
	 */
	public UnexpectedException( final String message ) {
		super( message );
	}

}
