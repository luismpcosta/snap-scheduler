package io.opensw.scheduler.core.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
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
import io.opensw.scheduler.core.common.VoidTask;
import io.opensw.scheduler.core.domain.audit.TaskAuditRepository;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.events.SnapEventListener;
import io.opensw.scheduler.core.events.obj.TaskRunEvent;
import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;

@RunWith( JUnitPlatform.class )
class ServiceEventListenerTest {

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private TaskAuditRepository taskAuditRepository;

	@Mock
	private SchedulerRepository schedulerRepository;

	private SnapTaskHandler snapTaskHandler;

	private SnapSchedulerProperties properties;

	private SnapEventListener serviceEventListener;

	protected DataSource dataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(
				"jdbc:h2:mem:snap;DB_CLOSE_DELAY=-1;init=runscript from 'classpath:schema.sql'"
		);
		dataSource.setUser( "sa" );
		dataSource.setPassword( "sa" );
		return dataSource;
	}

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks( this );
		properties = new SnapSchedulerProperties();
		properties.setDbPollingInterval( Duration.ofSeconds( 60 ) );
		properties.setEnabled( true );

		snapTaskHandler = new SnapTaskHandler(
				schedulerRepository, properties, context, applicationEventPublisher
		);

		serviceEventListener = new SnapEventListener(
				taskAuditRepository, schedulerRepository, snapTaskHandler, properties
		);
	}

	@Test
	void scheduleSuccess() throws DatabaseException {
		OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.name( "Task Test" ).runAt( Instant.now().plusSeconds( 720 ) );

		serviceEventListener.onTaskRunEventFired(
				TaskRunEvent.create( oneTimeTask, "server-test", Instant.now().minusSeconds( 360 ), Instant.now() )
		);

		RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 60 ) ).name( "Task Test" ).runAt( Instant.now() );

		serviceEventListener.onTaskRunEventFired(
				TaskRunEvent.create( recurringTask, "server-test", Instant.now().minusSeconds( 360 ), Instant.now() )
		);
		
		verify( taskAuditRepository, times( 2 ) ).auditTask(
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(),
				Mockito.any()
		);

	}

}
