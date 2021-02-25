package io.opensw.scheduler.core.domain.audit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.utils.DbUtils;

@RunWith( JUnitPlatform.class )
class TaskAuditRepositoryTest {

	private TaskAuditRepository taskAuditRepository;

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
		taskAuditRepository = new TaskAuditRepositoryImpl( dataSource(), new ObjectMapper() );
	}

	@Test
	void insertAuditSuccess() throws SQLException, DatabaseException {
		boolean result = taskAuditRepository.auditTask(
				UUID.randomUUID().toString(), "io.opensw.scheduler", "server-test", Instant.now(),
				Instant.now().plus( 1, ChronoUnit.HOURS ), null
		);

		assertTrue( result );
	}

	@Test
	void insertAudit_DatabaseException() throws SQLException, DatabaseException {
		Assertions.assertThrows( DatabaseException.class, () -> {
			new TaskAuditRepositoryImpl( null, new ObjectMapper() ).auditTask(
					UUID.randomUUID().toString(), "io.opensw.scheduler", "server-test", Instant.now(),
					Instant.now().plus( 1, ChronoUnit.HOURS ), null
			);
		} );
	}

	@Test
	void insertAudit_PK_NULL() throws SQLException, DatabaseException {
		boolean result = taskAuditRepository.auditTask(
				null, "io.opensw.scheduler", "server-test", Instant.now(),
				Instant.now().plus( 1, ChronoUnit.HOURS ), null
		);

		assertFalse( result );
	}

	@Test
	void insertQuery() throws SQLException, DatabaseException {
		// DbUtils.DB_MYSQL
		Object query = ReflectionTestUtils
				.invokeMethod( taskAuditRepository, TaskAuditRepositoryImpl.class, "insertQuery", DbUtils.DB_MYSQL );
		assertEquals( TaskAuditRepositoryImpl.INSERT_QUERY, query.toString() );

		// DbUtils.DB_MARIADB
		query = ReflectionTestUtils
				.invokeMethod( taskAuditRepository, TaskAuditRepositoryImpl.class, "insertQuery", DbUtils.DB_MARIADB );
		assertEquals( TaskAuditRepositoryImpl.INSERT_QUERY, query.toString() );

		// DbUtils.DB_MSSQL_SERVER
		query = ReflectionTestUtils
				.invokeMethod(
						taskAuditRepository, TaskAuditRepositoryImpl.class, "insertQuery", DbUtils.DB_MSSQL_SERVER
				);
		assertEquals( TaskAuditRepositoryImpl.INSERT_QUERY, query.toString() );

		// DbUtils.DB_H2
		query = ReflectionTestUtils
				.invokeMethod(
						taskAuditRepository, TaskAuditRepositoryImpl.class, "insertQuery", DbUtils.DB_H2
				);
		assertEquals( TaskAuditRepositoryImpl.INSERT_QUERY, query.toString() );

		// DbUtils.DB_POSTGRESQL
		query = ReflectionTestUtils.invokeMethod(
				taskAuditRepository, TaskAuditRepositoryImpl.class, "insertQuery", DbUtils.DB_POSTGRESQL
		);
		assertEquals( TaskAuditRepositoryImpl.POSTGRE_INSERT_QUERY, query.toString() );

	}
	
}
