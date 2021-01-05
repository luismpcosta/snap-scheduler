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
import io.opensw.scheduler.core.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LockRepositoryImpl implements LockRepository {

	private final DataSource dataSource;

	private final String dbPlatform;

	// select task and lock
	protected static final String MYSQL_LOCK_SELECT_QUERY = "SELECT `key`, `method`, lock_until, lock_at, lock_by FROM snap_lock WHERE `key` = ? AND `method` = ? FOR UPDATE";

	protected static final String POSTGRE_LOCK_SELECT_QUERY = "SELECT key, method, lock_until, lock_at, lock_by FROM snap_lock WHERE key = ? AND method = ? FOR UPDATE";

	protected static final String MSSQL_LOCK_SELECT_QUERY = "SELECT [key], method, lock_until, lock_at, lock_by FROM snap_lock WHERE [key] = ? AND method = ? FOR UPDATE";

	protected static final String H2_LOCK_SELECT_QUERY = "SELECT key, method, lock_until, lock_at, lock_by FROM snap_lock WHERE key = ? AND method = ? FOR UPDATE";

	// insert lock
	protected static final String MYSQL_LOCK_INSERT_QUERY = "INSERT INTO snap_lock (`key`, `method`, lock_until, lock_at, lock_by) VALUES (?, ?, ?, ?, ?);";

	protected static final String POSTGRE_LOCK_INSERT_QUERY = "INSERT INTO snap_lock (key, method, lock_until, lock_at, lock_by) VALUES (?, ?, ?, ?, ?);";

	protected static final String MSSQL_LOCK_INSERT_QUERY = "INSERT INTO snap_lock ([key], method, lock_until, lock_at, lock_by) VALUES (?, ?, ?, ?, ?);";

	protected static final String H2_LOCK_INSERT_QUERY = "INSERT INTO snap_lock (key, method, lock_until, lock_at, lock_by) VALUES (?, ?, ?, ?, ?);";

	@Autowired
	public LockRepositoryImpl( @Qualifier("snapDataSource") final DataSource dataSource ) {
		this.dataSource = dataSource;
		this.dbPlatform = DbUtils.databaseType( dataSource );
	}

	@Override
	public boolean lockTask( final String key, final String method, final long time, final String server )
			throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement(
					this.selectForUpdateQuery( this.dbPlatform ), ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE
			);

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

		return this.insertLock( key, method, time, server );
	}

	private boolean insertLock( final String key, final String method, final long time, final String server ) {
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.insertQuery( this.dbPlatform ) );
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

	private String selectForUpdateQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_LOCK_SELECT_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_LOCK_SELECT_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_LOCK_SELECT_QUERY;

		case DbUtils.DB_H2:
			return H2_LOCK_SELECT_QUERY;

		default:
			return POSTGRE_LOCK_SELECT_QUERY;
		}
	}

	private String insertQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_LOCK_INSERT_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_LOCK_INSERT_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_LOCK_INSERT_QUERY;

		case DbUtils.DB_H2:
			return H2_LOCK_INSERT_QUERY;

		default:
			return POSTGRE_LOCK_INSERT_QUERY;
		}
	}
}
