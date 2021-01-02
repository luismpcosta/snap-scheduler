package io.opensw.scheduler.core.domain.audit;

import java.time.Instant;

import io.opensw.scheduler.core.exceptions.DatabaseException;

public interface TaskAuditRepository {

	/**
	 * 
	 * @param key       that identifies task
	 * @param method    class and method where task was running
	 * @param server    identification (server hostname)
	 * @param start     is the instant that task start running
	 * @param end       is the instant that task end running
	 * @param exception occurred during run
	 * @return result of operation 
	 * @throws DatabaseException that was thrown when do not have database
	 *                           configured
	 */
	boolean auditTask( String key, String method, String server, Instant start, Instant end, Exception exception )
			throws DatabaseException;

}
