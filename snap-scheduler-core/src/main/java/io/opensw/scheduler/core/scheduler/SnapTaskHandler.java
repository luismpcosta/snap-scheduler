package io.opensw.scheduler.core.scheduler;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.exceptions.UnexpectedException;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapTaskHandler {

	private final SchedulerRepository schedulerRepository;

	private final SnapSchedulerProperties properties;

	private final ApplicationContext context;

	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Handle tasks from database
	 */
	public void handleDbTasks() {
		log.debug( "SnapTaskHandler.handleDbTasks start handle tasks" );

		try {
			final List< Task > tasks = schedulerRepository
					.loadTasksToRun( ServerUtils.loadServerName(), properties.dbPollingInterval() );
			for ( Task task : tasks ) {
				this.handleTask( task );
			}
		}
		catch ( Exception e ) {
			log.error(
					"(SnapTaskHandler.handleDbTasks) error occure when trie to load tasks to run: Error: {}",
					e.getMessage()
			);
		}
	}

	/**
	 * Handle task
	 * 
	 * @param task to handle
	 * @throws UnexpectedException unexpected exception occur
	 */
	public void handleTask( final Task task ) throws UnexpectedException {
		try {
			// schedule a task to specific time
			final Timer timer = new Timer();
			if ( task.getRunAt().isBefore( Instant.now() ) ) {
				task.runAt( Instant.now().plusSeconds( 30 ) );
			}
			timer.schedule(
					new TimerTaskRunner( task, context, applicationEventPublisher ), Date.from( task.getRunAt() )
			);
		}
		catch ( Exception e ) {
			log.error( "(SnapTaskHandler.handleTask) Error on handle task. Error: ", e.getMessage() );

			throw new UnexpectedException( e.getMessage() );
		}
	}

}
