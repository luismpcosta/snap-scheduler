package io.opensw.scheduler.core.domain.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensw.scheduler.core.common.Email;
import io.opensw.scheduler.core.common.EmailTask;
import io.opensw.scheduler.core.common.VoidTask;
import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.utils.DbUtils;

@RunWith( JUnitPlatform.class )
class SchedulerRepositoryTest {

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
		schedulerRepository = new SchedulerRepositoryImpl( dataSource(), new ObjectMapper() );
	}

	@Test
	void loadTasksToRun_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new SchedulerRepositoryImpl( null, new ObjectMapper() ).loadTasksToRun( "server-test", 60000 );
		} );
	}

	@Test
	void insertTask_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new SchedulerRepositoryImpl( null, new ObjectMapper() ).insertTask( null );
		} );
	}

	@Test
	void updateTask_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new SchedulerRepositoryImpl( null, new ObjectMapper() )
					.updateTask( UUID.randomUUID().toString(), Instant.now() );
		} );
	}

	@Test
	void updateRecurringTask_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new SchedulerRepositoryImpl( null, new ObjectMapper() ).updateRecurringTask( null, true, "" );
		} );
	}

	@Test
	void updateNotRunnedTask_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new SchedulerRepositoryImpl( null, new ObjectMapper() ).updateNotRunnedTask( "server-test" );
		} );
	}

	@Test
	void insertAndLoadTasksSuccess() throws DatabaseException {
		List< Task > results = schedulerRepository.loadTasksToRun( "server-test", 60000 );

		assertTrue( results.isEmpty() );

		results = schedulerRepository.loadTasksToRun( null, 60000 );

		assertTrue( results.isEmpty() );

		Email email = new Email();
		email.setEmail( "luismpcosta@gmail.com" );

		OneTimeTask oneTimeTask = OneTimeTask.create( EmailTask.class ).data( email ).dataClazz( Email.class )
				.key( UUID.randomUUID().toString() ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		schedulerRepository.insertTask( oneTimeTask );

		results = schedulerRepository.loadTasksToRun( "server-test", 60000 );

		assertFalse( results.isEmpty() );

		RecurringTask recurringTask = RecurringTask.create( EmailTask.class ).data( email ).dataClazz( Email.class )
				.key( UUID.randomUUID().toString() ).recurrence( Duration.ofSeconds( 60 ) ).name( "Task Test" )
				.runAt( Instant.now().plusSeconds( 60 ) );

		schedulerRepository.insertTask( recurringTask );

		results = schedulerRepository.loadTasksToRun( "server-test", 60000 );

		assertFalse( results.isEmpty() );
	}

	@Test
	void updateSuccess() throws DatabaseException {
		boolean result = schedulerRepository.updateTask( "not-exists-key", Instant.now() );

		assertFalse( result );

		final String key = UUID.randomUUID().toString();
		OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( key ).name( "Task Test" )
				.runAt( Instant.now().plusSeconds( 60 ) );

		schedulerRepository.insertTask( oneTimeTask );

		result = schedulerRepository.updateTask( key, Instant.now() );
		assertTrue( result );
	}

	@Test
	void updateRecurringTask() throws DatabaseException {
		RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 60 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		boolean result = schedulerRepository.updateRecurringTask( recurringTask, false, null );

		assertFalse( result );

		schedulerRepository.insertTask( recurringTask );

		result = schedulerRepository.updateRecurringTask( recurringTask, false, null );
		
		assertTrue( result );
	}

	@Test
	void updateNotRunnedTask() throws DatabaseException {
		boolean result = schedulerRepository.updateNotRunnedTask( "server-test" );
		
		assertFalse( result );
		
		RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( UUID.randomUUID().toString() )
				.recurrence( Duration.ofSeconds( 60 ) ).name( "Task Test" ).runAt( Instant.now().plusSeconds( 60 ) );

		schedulerRepository.insertTask( recurringTask );
		
		schedulerRepository.updateRecurringTask( recurringTask, true, "server-test" );
		
		result = schedulerRepository.updateNotRunnedTask( "server-test" );
		
		assertTrue( result );
	}

	@Test
	void updateRecurringTaskQuery() {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateRecurringTaskQuery", DbUtils.DB_MYSQL
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_RECURRING_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateRecurringTaskQuery",
						DbUtils.DB_MARIADB
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_RECURRING_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateRecurringTaskQuery",
						DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( SchedulerRepositoryImpl.MSSQL_UPDATE_RECURRING_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateRecurringTaskQuery", DbUtils.DB_H2
				);
		assertEquals( SchedulerRepositoryImpl.H2_UPDATE_RECURRING_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				schedulerRepository, SchedulerRepositoryImpl.class, "updateRecurringTaskQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( SchedulerRepositoryImpl.POSTGRE_UPDATE_RECURRING_QUERY, query.toString() );

	}

	@Test
	void updateNotRunnedQuery() {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateNotRunnedQuery", DbUtils.DB_MYSQL
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_NOT_RUN_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateNotRunnedQuery", DbUtils.DB_MARIADB
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_NOT_RUN_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateNotRunnedQuery",
						DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( SchedulerRepositoryImpl.MSSQL_UPDATE_NOT_RUN_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateNotRunnedQuery", DbUtils.DB_H2
				);
		assertEquals( SchedulerRepositoryImpl.H2_UPDATE_NOT_RUN_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				schedulerRepository, SchedulerRepositoryImpl.class, "updateNotRunnedQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( SchedulerRepositoryImpl.POSTGRE_UPDATE_NOT_RUN_QUERY, query.toString() );

	}

	@Test
	void selectTasksForUpdateQuery() {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "selectTasksForUpdateQuery",
						DbUtils.DB_MYSQL
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_SELECT_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "selectTasksForUpdateQuery",
						DbUtils.DB_MARIADB
				);
		assertEquals( SchedulerRepositoryImpl.MYSQL_SELECT_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "selectTasksForUpdateQuery",
						DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( SchedulerRepositoryImpl.MSSQL_SELECT_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "selectTasksForUpdateQuery", DbUtils.DB_H2
				);
		assertEquals( SchedulerRepositoryImpl.H2_SELECT_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				schedulerRepository, SchedulerRepositoryImpl.class, "selectTasksForUpdateQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( SchedulerRepositoryImpl.POSTGRE_SELECT_QUERY, query.toString() );

	}

	@Test
	void insertQuery() {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod( schedulerRepository, SchedulerRepositoryImpl.class, "insertQuery", DbUtils.DB_MYSQL );
		assertEquals( SchedulerRepositoryImpl.MYSQL_INSERT_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod( schedulerRepository, SchedulerRepositoryImpl.class, "insertQuery", DbUtils.DB_MARIADB );
		assertEquals( SchedulerRepositoryImpl.MYSQL_INSERT_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "insertQuery", DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( SchedulerRepositoryImpl.MSSQL_INSERT_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "insertQuery", DbUtils.DB_H2
				);
		assertEquals( SchedulerRepositoryImpl.H2_INSERT_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				schedulerRepository, SchedulerRepositoryImpl.class, "insertQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( SchedulerRepositoryImpl.POSTGRE_INSERT_QUERY, query.toString() );

	}

	@Test
	void updateQuery() {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod( schedulerRepository, SchedulerRepositoryImpl.class, "updateQuery", DbUtils.DB_MYSQL );
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod( schedulerRepository, SchedulerRepositoryImpl.class, "updateQuery", DbUtils.DB_MARIADB );
		assertEquals( SchedulerRepositoryImpl.MYSQL_UPDATE_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateQuery", DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( SchedulerRepositoryImpl.MSSQL_UPDATE_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						schedulerRepository, SchedulerRepositoryImpl.class, "updateQuery", DbUtils.DB_H2
				);
		assertEquals( SchedulerRepositoryImpl.H2_UPDATE_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				schedulerRepository, SchedulerRepositoryImpl.class, "updateQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( SchedulerRepositoryImpl.POSTGRE_UPDATE_QUERY, query.toString() );

	}
}
