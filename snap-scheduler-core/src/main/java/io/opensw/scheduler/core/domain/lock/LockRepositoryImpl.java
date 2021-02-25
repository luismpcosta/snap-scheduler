package io.opensw.scheduler.core.domain.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.exceptions.DatabaseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LockRepositoryImpl implements LockRepository {

	private final DataSource dataSource;

	// select task and lock
	protected static final String LOCK_SELECT_QUERY = "SELECT task_key, task_method, lock_until, lock_at, lock_by FROM snap_lock WHERE task_key = ? AND task_method = ? FOR UPDATE";

	// insert lock
	protected static final String LOCK_INSERT_QUERY = "INSERT INTO snap_lock (task_key, task_method, lock_until, lock_at, lock_by) VALUES (?, ?, ?, ?, ?);";

	@Autowired
	public LockRepositoryImpl( @Qualifier( "snapDataSource" ) final DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	@Override
	public boolean lockTask( final String key, final String method, final long time, final String server )
			throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}

		try ( Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(
						LOCK_SELECT_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
				) ) {
			connection.setAutoCommit( true );

			preparedStatement.setString( 1, key );
			preparedStatement.setString( 2, method );

			ResultSet resultSet = preparedStatement.executeQuery();
			if ( resultSet.next() ) {
				// if exists a entry and stay in lock time
				if ( resultSet.getTimestamp( 3 ).toInstant().isAfter( Instant.now() ) ) {
					return false;
				}

				resultSet.updateTimestamp( 3, Timestamp.from( Instant.now().plus( time, ChronoUnit.SECONDS ) ) );
				resultSet.updateTimestamp( 4, Timestamp.from( Instant.now() ) );
				resultSet.updateString( 5, server );
				resultSet.updateRow();

				return true;
			}
		}
		catch ( Exception e ) {
			log.error(
					"(UPDATE) Can not acquire lock for key {} and method {}. Message error: {}", key, method,
					e.getMessage()
			);
		}

		return this.insertLock( key, method, time, server );
	}

	private boolean insertLock( final String key, final String method, final long time, final String server ) {
		try ( Connection connection = dataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement( LOCK_INSERT_QUERY ) ) {
			connection.setAutoCommit( true );

			preparedStatement.setString( 1, key );
			preparedStatement.setString( 2, method );
			preparedStatement.setTimestamp( 3, Timestamp.from( Instant.now().plus( time, ChronoUnit.SECONDS ) ) );
			preparedStatement.setTimestamp( 4, Timestamp.from( Instant.now() ) );
			preparedStatement.setString( 5, server );

			return preparedStatement.executeUpdate() == 1;
		}
		catch ( Exception e ) {
			log.error(
					"(INSERT) Can not acquire lock for key {} and method {}. Message error: {}", key, method,
					e.getMessage()
			);
		}

		return false;
	}

}
