package io.opensw.scheduler.core.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.exceptions.DatabaseException;

@RunWith( JUnitPlatform.class )
class DbPullRunnerTest {

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private SchedulerRepository schedulerRepository;

	private SnapSchedulerProperties properties;

	private SnapTaskHandler handler;

	private DbPullRunner runner;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks( this );
		properties = new SnapSchedulerProperties();
		properties.setDbPollingInterval( Duration.ofSeconds( 60 ) );
		properties.setEnabled( true );

		handler = new SnapTaskHandler( schedulerRepository, properties, context, applicationEventPublisher );

		runner = new DbPullRunner( handler );
	}

	@Test
	void run() throws InterruptedException, DatabaseException {
		runner.interval( 6 );
		runner.run();

		runner.interval( 60000 );

		Thread newThread = new Thread( () -> {
			runner.run();
		} );

		newThread.start();
		TimeUnit.SECONDS.sleep( 1 );
		runner.terminate();

		verify( schedulerRepository, times( 1 ) ).loadTasksToRun( Mockito.anyString(), Mockito.anyLong() );
	}
	
	@Test
	void runningFalse() throws InterruptedException, DatabaseException {
		runner.interval( 60000 );
		runner.terminate();

			runner.run();

		TimeUnit.SECONDS.sleep( 1 );

		verifyNoInteractions( schedulerRepository );
	}

}
