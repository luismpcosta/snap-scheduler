package io.opensw.scheduler.core.exceptions;

/**
 * TaskDefinitionException was throw when a task do not have right definition
 * 
 * @author luis.costa
 *
 */
public class TaskDefinitionException extends Exception {

	private static final long serialVersionUID = -8165716080219372852L;

	/**
	 * TaskDefinitionException constructor
	 */
	public TaskDefinitionException() {
		super( "Task with definition error." );
	}

	/**
	 * TaskDefinitionException constructor with custom message
	 * 
	 * @param message to instantiate exception
	 */
	public TaskDefinitionException( final String message ) {
		super( message );
	}

	/**
	 * Create instance of TaskDefinitionException
	 * 
	 * @param message to instantiate exception
	 * @return instance of TaskDefinitionException
	 */
	public static TaskDefinitionException create( final String message ) {
		return new TaskDefinitionException( message );
	}
}
