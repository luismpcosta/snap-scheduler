package io.opensw.scheduler.core.events.obj;

import java.time.Instant;

import io.opensw.scheduler.core.scheduler.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskRunEvent implements EventObj {

	/**
	 * Gets the task
	 *
	 * @return the task
	 */
	@Getter
	private final Task task;

	/**
	 * Gets the server
	 *
	 * @return the server
	 */
	@Getter
	private final String server;

	/**
	 * Gets the start time
	 *
	 * @return the start time
	 */
	@Getter
	private final Instant start;

	/**
	 * Gets the end time
	 *
	 * @return the end time
	 */
	@Getter
	private final Instant end;

	/**
	 * Gets the exception
	 *
	 * @return the exception
	 */
	@Getter
	private final Exception exception;

	/**
	 * Creates event task run event
	 * 
	 * @param task   is the task
	 * @param server is the name of server where task run
	 * @param start  is the start time
	 * @param end    is the end time
	 * @return instance of task run event
	 */
	public static TaskRunEvent create( final Task task, final String server, final Instant start, final Instant end ) {
		return new TaskRunEvent( task, server, start, end, null );
	}

	/**
	 * Creates event task run event
	 * 
	 * @param task      is the task
	 * @param server    is the name of server where task run
	 * @param start     is the start time
	 * @param end       is the end time
	 * @param exception when exception occur
	 * @return instance of task run event
	 */
	public static TaskRunEvent create( final Task task, final String server, final Instant start, final Instant end,
			final Exception exception ) {
		return new TaskRunEvent( task, server, start, end, exception );
	}
}
