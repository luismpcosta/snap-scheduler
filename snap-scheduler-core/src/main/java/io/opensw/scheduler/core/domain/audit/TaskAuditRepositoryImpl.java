package io.opensw.scheduler.core.domain.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.utils.DbUtils;
import io.opensw.scheduler.core.utils.SnapExceptionUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskAuditRepositoryImpl implements TaskAuditRepository {

	private final DataSource dataSource;

	private final String dbPlatform;

	private final ObjectMapper mapper;

	protected static final String MYSQL_INSERT_QUERY = "INSERT INTO snap_task_audit(`key`, `method`, run_on, start_run, end_run, run_time_seconds, task_error) VALUES (?, ?, ?, ?, ?, ?, ?);";

	protected static final String POSTGRE_INSERT_QUERY = "INSERT INTO snap_task_audit(key, method, run_on, start_run, end_run, run_time_seconds, task_error) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb);";

	protected static final String MSSQL_INSERT_QUERY = "INSERT INTO snap_task_audit([key], method, run_on, start_run, end_run, run_time_seconds, task_error) VALUES (?, ?, ?, ?, ?, ?, ?);";

	protected static final String H2_INSERT_QUERY = "INSERT INTO snap_task_audit(key, method, run_on, start_run, end_run, run_time_seconds, task_error) VALUES (?, ?, ?, ?, ?, ?, ?);";

	@Autowired
	public TaskAuditRepositoryImpl( @Qualifier("snapDataSource") final DataSource dataSource, final ObjectMapper mapper ) {
		this.dataSource = dataSource;
		this.mapper = mapper;
		this.dbPlatform = DbUtils.databaseType( dataSource );
	}

	@Override
	public boolean auditTask( final String key, final String method, final String server, final Instant start,
			final Instant end, final Exception exception ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.insertQuery( this.dbPlatform ) );
			preparedStatement.setString( 1, key );
			preparedStatement.setString( 2, method );
			preparedStatement.setString( 3, server );
			preparedStatement.setTimestamp( 4, Timestamp.from( start ) );
			preparedStatement.setTimestamp( 5, Timestamp.from( end ) );
			preparedStatement.setLong( 6, ( Duration.between( start, end ).toMillis() / 1000 ) );
			final String excptionStr = exception != null
					? mapper.writeValueAsString( SnapExceptionUtils.toMap( exception ) )
					: null;
			preparedStatement.setString( 7, excptionStr );

			return preparedStatement.executeUpdate() > 0;
		}
		catch ( Exception e ) {
			log.error(
					"(INSERT) Can not insert task audit with key {}, method {} and server {}. Message error: {}", key,
					method,
					server, e.getMessage()
			);
		}
		finally {
			if ( preparedStatement != null ) {
				try {
					preparedStatement.close();
				}
				catch ( Exception e ) {
					log.warn( DbUtils.ERROR_CLOSE_STMT_MSG, e.getMessage() );
				}
			}
		}
		return false;
	}

	private String insertQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_INSERT_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_INSERT_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_INSERT_QUERY;

		case DbUtils.DB_H2:
			return H2_INSERT_QUERY;

		default:
			return POSTGRE_INSERT_QUERY;
		}
	}
}
