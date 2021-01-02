package io.opensw.scheduler.core.scheduler;

import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapSchedulerRunner implements CommandLineRunner {

	private final DbPullRunner dbPullRunner;

	private final SchedulerRepository schedulerRepository;

	private final SnapSchedulerProperties properties;

	/**
	 * Run after application start
	 */
	@Override
	public void run( String... arg0 ) throws Exception {
		log.debug( "#=> Start SnapSchedulerRunner #" );

		if ( properties.isEnabled() ) {
			dbPullRunner.interval( properties.dbPollingInterval() );

			dbPullRunner.start();
		}
	}

	/**
	 * On exit application change picked tasks to false
	 * @throws DatabaseException thrown when database not configured well
	 */
	@PreDestroy
	public void onExit() throws DatabaseException {
		log.debug( "#=> Call SnapSchedulerRunner exit runner #" );
		
		// stop db pull thread
		dbPullRunner.terminate();
		
		// update not runned tasks
		schedulerRepository.updateNotRunnedTask( ServerUtils.loadServerName() );
		
		log.debug( "#=> SnapSchedulerRunner exit concluded #" );
	}

}
