package io.opensw.scheduler.core.scheduler.task;

public abstract class TaskDataExecutor<T extends TaskData> implements TaskExecutor {

	/**
	 * Abstract method with execution task code
	 * 
	 * @param data object with task data
	 */
	public abstract void execute( T data );

	@Override
	public void execute() {
		// do nothing
	}
	

}
