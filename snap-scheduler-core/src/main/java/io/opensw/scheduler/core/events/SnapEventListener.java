package io.opensw.scheduler.core.events;

import java.time.Duration;
import java.time.Instant;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.audit.TaskAuditRepository;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.events.obj.TaskRunEvent;
import io.opensw.scheduler.core.scheduler.SnapTaskHandler;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.scheduler.task.TaskType;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service event listener
 * 
 * @author luis.costa
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapEventListener {

	private final TaskAuditRepository taskAuditRepository;

	private final SchedulerRepository schedulerRepository;

	private final SnapTaskHandler snapTaskHandler;

	private final SnapSchedulerProperties properties;

	/**
	 * On task run
	 *
	 * @param event the task run event
	 */
	@Async
	@EventListener( classes = TaskRunEvent.class )
	public void onTaskRunEventFired( final TaskRunEvent event ) {
		log.debug( "Listener to TaskRunEvent {}", event );

		final Task task = event.getTask();
		// create audit entry in database
		try {
			taskAuditRepository.auditTask(
					task.getKey(), task.getClazz().getName(), event.getServer(), event.getStart(), event.getEnd(),
					event.getException()
			);
		}
		catch ( Exception e ) {
			log.error(
					"Error in save running task event with key {}, method {} and server {}. Error: {}", task.getKey(),
					task.getClazz().getName(), event.getServer(), e.getMessage()
			);
		}

		// update task end time or recurring time
		try {
			if ( TaskType.RECURRING.equals( task.getType() ) && task instanceof RecurringTask ) {
				final RecurringTask recurringTask = (RecurringTask) task;

				// data to schedule task
				boolean picked = false;
				String pickedBy = null;

				// calculate next task running time
				recurringTask.runAt( recurringTask.getRunAt().plus( recurringTask.getRecurrence() ) );
				long diff = Duration.between( Instant.now(), recurringTask.getRunAt() ).toMillis();
				if ( diff < ( properties.dbPollingInterval() * 2 ) ) {
					snapTaskHandler.handleTask( recurringTask );

					// update picked data
					picked = true;
					pickedBy = ServerUtils.loadServerName();
				}

				// update recurring task on database
				schedulerRepository.updateRecurringTask( recurringTask, picked, pickedBy );
			}
			else {
				schedulerRepository.updateTask( task.getKey(), event.getEnd() );
			}
		}
		catch ( Exception e ) {
			log.error(
					"(ServiceEventListener.onTaskRunEventFired) Error on update runned task with key {}.",
					task.getKey()
			);
		}
	}

}
