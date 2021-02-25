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
import io.opensw.scheduler.core.utils.SnapExceptionUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskAuditRepositoryImpl implements TaskAuditRepository {

	private final DataSource dataSource;

	private final ObjectMapper mapper;

	protected static final String INSERT_QUERY = "INSERT INTO snap_task_audit(task_key, task_method, run_on, start_run, end_run, run_time_seconds, task_error) VALUES (?, ?, ?, ?, ?, ?, ?);";

	@Autowired
	public TaskAuditRepositoryImpl( @Qualifier( "snapDataSource" ) final DataSource dataSource,
			final ObjectMapper mapper ) {
		this.dataSource = dataSource;
		this.mapper = mapper;
	}

	@Override
	public boolean auditTask( final String key, final String method, final String server, final Instant start,
			final Instant end, final Exception exception ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}

		try ( Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement( INSERT_QUERY ) ) {
			connection.setAutoCommit( true );

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
		
		return false;
	}

}
