package io.opensw.scheduler.core.domain.scheduler;

import java.time.Instant;
import java.util.List;

import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;

public interface SchedulerRepository {

	/**
	 * Save task on database
	 * 
	 * @param task to save on database
	 * @return result of operation
	 * @exception DatabaseException thrown when database not configured well
	 */
	boolean insertTask( Task task ) throws DatabaseException;

	/**
	 * Save task on database
	 * 
	 * @param task   to save on database
	 * @param picked boolean with picked status
	 * @param server is the name/identifier of server
	 * @return result of operation
	 * @exception DatabaseException thrown when database not configured well
	 */
	boolean insertTask( Task task, boolean picked, String server ) throws DatabaseException;

	/**
	 * Load tasks to run
	 * 
	 * @param server          identifier
	 * @param pollingInterval interval in milliseconds to pull tasks
	 * @return list of tasks to run
	 * @throws DatabaseException exception thrown when database no configured
	 *                           well
	 */
	List< Task > loadTasksToRun( String server, long pollingInterval ) throws DatabaseException;

	/**
	 * Update task data after run
	 * 
	 * @param key task identifier
	 * @param end time
	 * @return result of operation
	 * @throws DatabaseException thrown when database not configured well
	 */
	boolean updateTask( String key, Instant end ) throws DatabaseException;

	/**
	 * Update recurring task data after run This update running fields to put
	 * task available to run another time
	 * 
	 * Changed fields: picked = false, picked_by = null and end_run = null
	 * 
	 * @param task to update
	 * @param picked if task was picked by this instance
	 * @param pickedBy server name
	 * @return result of operation
	 * @throws DatabaseException thrown when database not configured well
	 */
	boolean updateRecurringTask( RecurringTask task, boolean picked, String pickedBy ) throws DatabaseException;

	/**
	 * Update not runned tasks to make them available to be picked by another
	 * servers
	 * 
	 * @param server identifier
	 * @return result of operation
	 * @throws DatabaseException thrown when database not configured well
	 */
	boolean updateNotRunnedTask( String server ) throws DatabaseException;


}
