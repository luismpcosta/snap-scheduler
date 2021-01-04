package io.opensw.scheduler.core.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbPullRunner extends Thread {

	private final SnapTaskHandler handler;

	private boolean running = true;

	private long interval;

	@Override
	public void run() {
		log.debug( "# Start Thread SnapSchedulerRunner" );

		if ( interval >= 60000 ) {
			try {
				while ( running ) {
					log.debug( "Handle tasks from database" );

					handler.handleDbTasks();

					log.debug( "End handling tasks from database" );

					TimeUnit.MILLISECONDS.sleep( interval );
				}
			}
			catch ( Exception e ) {
				log.error( "Error on SnapSchedulerRunner polling thread running. Error: {}", e.getMessage() );
			}
		}
		else {
			log.error( "Scheduler database polling not started because db polling tim is less than 1 minute." );
		}

		log.debug( "# END Thread SnapSchedulerRunner" );
	}

	/**
	 * Set running to false to thread exit
	 */
	public synchronized void terminate() {
		this.running = false;
	}

	/**
	 * Change interval duration
	 * 
	 * @param interval value to change
	 */
	public synchronized void interval( long interval ) {
		this.interval = interval;
	}

}
