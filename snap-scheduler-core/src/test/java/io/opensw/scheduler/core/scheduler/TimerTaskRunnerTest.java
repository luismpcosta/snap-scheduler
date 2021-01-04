package io.opensw.scheduler.core.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.common.Email;
import io.opensw.scheduler.core.common.EmailTask;
import io.opensw.scheduler.core.common.VoidTask;
import io.opensw.scheduler.core.events.obj.TaskRunEvent;
import io.opensw.scheduler.core.scheduler.TimerTaskRunner;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;

@RunWith( JUnitPlatform.class )
class TimerTaskRunnerTest {

	private ApplicationContext context;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private SnapSchedulerProperties properties;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks( this );
		context = Mockito.mock( ConfigurableApplicationContext.class );

		properties = new SnapSchedulerProperties();
		properties.setDbPollingInterval( Duration.ofSeconds( 60 ) );
		properties.setEnabled( true );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void oneTimeTask() {
		when( context.getBean( Mockito.any( Class.class ) ) ).thenReturn( new VoidTask() );

		OneTimeTask task = OneTimeTask.create( VoidTask.class ).key( UUID.randomUUID().toString() ).name( "Task Test" )
				.runAt( Instant.now().plusSeconds( 5 ) );

		TimerTaskRunner runner = new TimerTaskRunner( task, context, applicationEventPublisher );
		runner.run();

		verify( applicationEventPublisher, times( 1 ) ).publishEvent( Mockito.any( TaskRunEvent.class ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void recurringTask() {
		when( context.getBean( Mockito.any( Class.class ) ) ).thenReturn( new Exception(), new VoidTask() );

		RecurringTask task = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 5 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		TimerTaskRunner runner = new TimerTaskRunner( task, context, applicationEventPublisher );
		runner.run();

		verify( applicationEventPublisher, times( 1 ) ).publishEvent( Mockito.any( TaskRunEvent.class ) );
	}
	
	@Test
	@SuppressWarnings( "unchecked" )
	void recurringTaskData() {
		when( context.getBean( Mockito.any( Class.class ) ) ).thenReturn( new VoidTask() );

		final Email data = new Email();
		data.setEmail( "email@opensw.io" );
		
		RecurringTask task = RecurringTask.create( EmailTask.class ).data( data ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 5 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		TimerTaskRunner runner = new TimerTaskRunner( task, context, applicationEventPublisher );
		runner.run();

		verify( applicationEventPublisher, times( 1 ) ).publishEvent( Mockito.any( TaskRunEvent.class ) );

		//do nothing it's only to pass in override code
		new EmailTask().execute();
	}
	
	@Test
	void notRun() {
		RecurringTask task = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 5 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		TimerTaskRunner runner = new TimerTaskRunner( task, context, applicationEventPublisher );
		runner.run();

		verify( applicationEventPublisher, times( 1 ) ).publishEvent( Mockito.any( TaskRunEvent.class ) );
	}

}
