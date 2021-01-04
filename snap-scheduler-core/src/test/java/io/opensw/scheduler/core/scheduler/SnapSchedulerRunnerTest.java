package io.opensw.scheduler.core.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;

@RunWith( JUnitPlatform.class )
class SnapSchedulerRunnerTest {

	@Mock
	private DbPullRunner dbPullRunner;

	@Mock
	private SchedulerRepository schedulerRepository;

	private SnapSchedulerProperties properties;

	private SnapSchedulerRunner snapSchedulerRunner;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks( this );

		properties = new SnapSchedulerProperties();
		properties.setDbPollingInterval( Duration.ofSeconds( 60 ) );
		properties.setEnabled( true );
		
		snapSchedulerRunner= new SnapSchedulerRunner( dbPullRunner, schedulerRepository, properties );
	}

	@Test
	void run() throws Exception {
		snapSchedulerRunner.run();
		

		verify( dbPullRunner, times( 1 ) ).interval( Mockito.anyLong() );
		verify( dbPullRunner, times( 1 ) ).start();
	}

	@Test
	void notRun() throws Exception {
		properties.setEnabled( false );
		
		snapSchedulerRunner.run();
		
		
		verify( dbPullRunner, times( 0 ) ).interval( Mockito.anyLong() );
		verify( dbPullRunner, times( 0 ) ).start();
	}
	
	@Test
	void onExit() throws Exception {
		
		snapSchedulerRunner.onExit();
		
		
		verify( dbPullRunner, times( 1 ) ).terminate();
		verify( schedulerRepository, times( 1 ) ).updateNotRunnedTask( Mockito.anyString() );
	}

}
