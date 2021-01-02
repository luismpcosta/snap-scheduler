package io.opensw.scheduler.core.exceptions;

/**
 * DatabaseException was throw when have an error in database
 * 
 * @author luis.costa
 *
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = -2087373241887772302L;

	/**
	 * DatabaseException constructor
	 */
	public DatabaseException() {
		super("Database not configured.");
	}

	/**
	 * DatabaseException constructor with custom message
	 * 
	 * @param message to instantiate exception
	 */
	public DatabaseException( final String message ) {
		super( message );
	}

}
