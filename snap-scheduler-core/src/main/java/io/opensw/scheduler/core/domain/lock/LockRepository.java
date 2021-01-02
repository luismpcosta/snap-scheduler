package io.opensw.scheduler.core.domain.lock;

import io.opensw.scheduler.core.exceptions.DatabaseException;

public interface LockRepository {

	/**
	 * Acquire task lock from database
	 * 
	 * @param key that identifies task
	 * @param method class and method where task was running
	 * @param time to lock task
	 * @param server identification (server hostname)
	 * @return true if can acquire lock or false if not
	 * @throws DatabaseException that was thrown when do not have database configured
	 */
	boolean lockTask( String key, String method, long time, String server ) throws DatabaseException;

}
