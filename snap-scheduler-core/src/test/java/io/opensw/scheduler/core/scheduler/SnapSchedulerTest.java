package io.opensw.scheduler.core.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensw.scheduler.SnapSchedulerProperties;
import io.opensw.scheduler.core.common.VoidTask;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepository;
import io.opensw.scheduler.core.domain.scheduler.SchedulerRepositoryImpl;
import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.exceptions.TaskDefinitionException;
import io.opensw.scheduler.core.exceptions.UnexpectedException;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;

@RunWith( JUnitPlatform.class )
class SnapSchedulerTest {

	@Mock
	private ApplicationContext context;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private SnapSchedulerProperties properties;

	private SnapScheduler snapScheduler;

	private SchedulerRepository schedulerRepository;

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

		schedulerRepository = new SchedulerRepositoryImpl( dataSource(), new ObjectMapper() );
		SnapTaskHandler snapTaskHandler = new SnapTaskHandler(
				schedulerRepository, properties, context, applicationEventPublisher
		);
		snapScheduler = new SnapScheduler( schedulerRepository, snapTaskHandler, properties );
	}

	@Test
	void scheduleSuccess() throws TaskDefinitionException, UnexpectedException, DatabaseException {
		OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.name( "Task Test" ).runAt( Instant.now().plusSeconds( 720 ) );

		snapScheduler.schedule( oneTimeTask );

		RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofDays( 30 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 720 ) );

		snapScheduler.schedule( recurringTask );

		List< Task > tasks = schedulerRepository.loadTasksToRun( "server-test", 400000 );

		assertFalse( tasks.isEmpty() );
	}

	@Test
	void scheduleSuccessAlreadyPicked() throws TaskDefinitionException, UnexpectedException, DatabaseException {
		OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.name( "Task Test" ).runAt( Instant.now().plusSeconds( 360 ) );

		snapScheduler.schedule( oneTimeTask );

		RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofDays( 30 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 360 ) );

		snapScheduler.schedule( recurringTask );

		List< Task > tasks = schedulerRepository.loadTasksToRun( "server-test", 400000 );

		assertTrue( tasks.isEmpty() );
	}

	@Test
	void validateTaskDefinition() {
		// Validate not assignable class
		OneTimeTask task = OneTimeTask.create( Object.class );
		Exception exception = Assertions.assertThrows( UndeclaredThrowableException.class, () -> {
			ReflectionTestUtils.invokeMethod( snapScheduler, SnapScheduler.class, "validateTaskDefinition", task );
		} );

		assertTrue( exception.getCause().getMessage().contains( "need to implement interface" ) );

		// start validation fields
		// validate runAt
		RecurringTask recurringTask = RecurringTask.create( VoidTask.class );
		exception = Assertions.assertThrows( UndeclaredThrowableException.class, () -> {
			ReflectionTestUtils
					.invokeMethod( snapScheduler, SnapScheduler.class, "validateTaskDefinition", recurringTask );
		} );

		assertEquals( "Task \"runAt\" field is required, please define it.", exception.getCause().getMessage() );

		// validate
		recurringTask.runAt( Instant.now().plusSeconds( 360 ) );
		exception = Assertions.assertThrows( UndeclaredThrowableException.class, () -> {
			ReflectionTestUtils
					.invokeMethod( snapScheduler, SnapScheduler.class, "validateTaskDefinition", recurringTask );
		} );

		assertEquals(
				"Task \"recurrence\" field is required for recurring tasks, please define it.",
				exception.getCause().getMessage()
		);

		// run ok
		recurringTask.recurrence( Duration.ofDays( 7 ) );
		ReflectionTestUtils.invokeMethod( snapScheduler, SnapScheduler.class, "validateTaskDefinition", recurringTask );
	}

}
