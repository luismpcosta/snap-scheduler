package io.opensw.scheduler.core.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.exceptions.TaskDefinitionException;
import io.opensw.scheduler.core.exceptions.UnexpectedException;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.scheduler.task.TaskDataExecutor;
import io.opensw.scheduler.core.scheduler.task.TaskExecutor;
import io.opensw.scheduler.core.scheduler.task.TaskType;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapScheduler {

	private final SchedulerRepository schedulerRepository;

	private final SnapTaskHandler snapTaskHandler;

	private final SnapSchedulerProperties properties;

	/**
	 * Task schedule
	 * 
	 * @param task to schedule
	 * @throws TaskDefinitionException throw when have definition error
	 * @throws UnexpectedException     unexpected exception occur
	 */
	public void schedule( final Task task ) throws TaskDefinitionException, UnexpectedException {
		log.debug( "(SnapScheduler.schedule) Start schedule task with key {}.", task.getKey() );

		// validate task definition
		this.validateTaskDefinition( task );

		final long minutes = Duration.between( Instant.now(), task.getRunAt() ).toMinutes();

		try {
			boolean picked = false;
			String server = null;

			if ( minutes < 10 || minutes < ( properties.getDbPollingInterval().toMinutes() * 2 ) ) {
				snapTaskHandler.handleTask( task );
				picked = true;
				server = ServerUtils.loadServerName();
			}

			schedulerRepository.insertTask( task, picked, server );
		}
		catch ( Exception e ) {
			log.error( "Erro on schedule task {}. Error: {}", task.toString(), e.getMessage() );

			throw new UnexpectedException( e.getMessage() );
		}
	}

	private void validateTaskDefinition( final Task task ) throws TaskDefinitionException {
		// validate if task implements TaskExecutor interface
		if ( !TaskDataExecutor.class.isAssignableFrom( task.getClazz() )
				&& !TaskExecutor.class.isAssignableFrom( task.getClazz() ) ) {
			throw TaskDefinitionException.create(
					String.format(
							"Task class \"%s\" need to implement interface \"%s\"", task.getClazz().getCanonicalName(),
							TaskDataExecutor.class.getCanonicalName()
					)
			);
		}

		// validate key (task identifier and pkey in database)
		if ( task.getKey() == null || task.getKey().isEmpty() ) {
			throw TaskDefinitionException.create( "Task \"key\" field is required, please define it." );
		}

		// validate run time
		if ( task.getRunAt() == null ) {
			throw TaskDefinitionException.create( "Task \"runAt\" field is required, please define it." );
		}

		// validate running class
		if ( task.getClazz() == null ) {
			throw TaskDefinitionException.create( "Task \"clazz\" field is required, please define it." );
		}

		// validate type of task (OneTime or Recurring)
		if ( task.getType() == null ) {
			throw TaskDefinitionException.create( "Task \"type\" field is required, please define it." );
		}

		// when recurring task validate recurrence
		if ( TaskType.RECURRING.equals( task.getType() ) && ( (RecurringTask) task ).getRecurrence() == null ) {
			throw TaskDefinitionException
					.create( "Task \"recurrence\" field is required for recurring tasks, please define it." );
		}

		// validate name
		if ( task.getName() == null || task.getName().isEmpty() ) {
			throw TaskDefinitionException.create( "Task \"name\" field is required, please define it." );
		}

		// when data was defined validate if data clazz was defined to
		if ( task.getData() != null && task.getDataClazz() == null ) {
			throw TaskDefinitionException
					.create( "Task \"data\" was defined but \"dataClazz\" was null, please define \"dataClazz\"." );
		}

	}
}
