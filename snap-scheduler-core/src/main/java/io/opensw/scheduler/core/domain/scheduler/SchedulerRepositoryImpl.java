package io.opensw.scheduler.core.domain.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensw.scheduler.core.exceptions.DatabaseException;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.scheduler.task.TaskData;
import io.opensw.scheduler.core.scheduler.task.TaskType;
import io.opensw.scheduler.core.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SchedulerRepositoryImpl implements SchedulerRepository {

	private final DataSource dataSource;

	private final ObjectMapper mapper;

	private final String dbPlatform;

	// insert new task
	protected static final String MYSQL_INSERT_QUERY = "INSERT INTO snap_scheduler(name, `key`, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	protected static final String POSTGRE_INSERT_QUERY = "INSERT INTO snap_scheduler(name, key, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?);";

	protected static final String MSSQL_INSERT_QUERY = "INSERT INTO snap_scheduler(name, [key], type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	protected static final String H2_INSERT_QUERY = "INSERT INTO snap_scheduler(name, key, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	// select tasks in period and lock task
	protected static final String MYSQL_SELECT_QUERY = "SELECT name, `key`, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by FROM snap_scheduler where picked = false AND run_at < ? order by run_at asc FOR UPDATE;";

	protected static final String POSTGRE_SELECT_QUERY = "SELECT name, key, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by FROM snap_scheduler where picked = false AND run_at < ? order by run_at asc FOR UPDATE;";

	protected static final String MSSQL_SELECT_QUERY = "SELECT name, [key], type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by FROM snap_scheduler where picked = 0 AND run_at < ? order by run_at asc FOR UPDATE;";

	protected static final String H2_SELECT_QUERY = "SELECT name, key, type, task_class, task_data, task_data_class, run_at, recurrence, picked, picked_by FROM snap_scheduler where picked = 0 AND run_at < ? order by run_at asc FOR UPDATE;";

	// update task after run
	protected static final String MYSQL_UPDATE_QUERY = "UPDATE snap_scheduler SET end_run = ? WHERE `key` = ?;";

	protected static final String POSTGRE_UPDATE_QUERY = "UPDATE snap_scheduler SET end_run = ? WHERE key = ?;";

	protected static final String MSSQL_UPDATE_QUERY = "UPDATE snap_scheduler SET end_run = ? WHERE [key] = ?;";

	protected static final String H2_UPDATE_QUERY = "UPDATE snap_scheduler SET end_run = ? WHERE key = ?;";

	// update recurring task after run
	protected static final String MYSQL_UPDATE_RECURRING_QUERY = "UPDATE snap_scheduler SET run_at = ?, picked = ?, picked_by = ?, end_run = null WHERE `key` = ?;";

	protected static final String POSTGRE_UPDATE_RECURRING_QUERY = "UPDATE snap_scheduler SET run_at = ?, picked = ?, picked_by = ?, end_run = null WHERE key = ?;";

	protected static final String MSSQL_UPDATE_RECURRING_QUERY = "UPDATE snap_scheduler SET run_at = ?, picked = ?, picked_by = ?, end_run = null WHERE [key] = ?;";

	protected static final String H2_UPDATE_RECURRING_QUERY = "UPDATE snap_scheduler SET run_at = ?, picked = ?, picked_by = ?, end_run = null WHERE key = ?;";

	// update task after run
	protected static final String POSTGRE_UPDATE_NOT_RUN_QUERY = "UPDATE snap_scheduler SET  picked_by = null, picked = false WHERE picked = true AND end_run is null AND picked_by = ?;";

	protected static final String MYSQL_UPDATE_NOT_RUN_QUERY = "UPDATE snap_scheduler SET picked_by = null, picked = false WHERE `key` in (select `key` from snap_scheduler where picked = true AND end_run is null AND picked_by = ?);";

	protected static final String MSSQL_UPDATE_NOT_RUN_QUERY = "UPDATE snap_scheduler SET picked_by = null, picked = 0 WHERE picked = 1 AND end_run is null AND picked_by = ?;";

	protected static final String H2_UPDATE_NOT_RUN_QUERY = "UPDATE snap_scheduler SET picked_by = null, picked = 0 WHERE picked = 1 AND end_run is null AND picked_by = ?;";

	@Autowired
	public SchedulerRepositoryImpl( @Qualifier("snapDataSource") final DataSource dataSource, final ObjectMapper mapper ) {
		this.dataSource = dataSource;
		this.mapper = mapper;
		this.dbPlatform = DbUtils.databaseType( dataSource );
	}

	@Override
	public List< Task > loadTasksToRun( final String server, final long pollingInterval ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		final List< Task > tasks = new ArrayList<>();

		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement(
					this.selectTasksForUpdateQuery( this.dbPlatform ), ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE
			);

			preparedStatement
					.setTimestamp(
							1, Timestamp.from( Instant.now().plus( ( pollingInterval * 2 ), ChronoUnit.MILLIS ) )
					);

			ResultSet resultSet = preparedStatement.executeQuery();
			while ( resultSet.next() ) {
				final TaskType type = TaskType.valueOf( resultSet.getString( 3 ) );
				Task task = null;
				if ( TaskType.RECURRING.equals( type ) ) {
					task = this.buildRecurringTaskFromResultSet( resultSet );
				}
				else {
					task = this.buildOneTimeTaskFromResultSet( resultSet );
				}

				try {
					if ( task != null ) {
						tasks.add( task );
						resultSet.updateBoolean( 9, true );
						resultSet.updateString( 10, server );
						resultSet.updateRow();
					}
				}
				catch ( Exception e ) {
					log.error( "Can not update task and remove from list of tasks." );
					tasks.remove( task );
				}

			}
		}
		catch ( Exception e ) {
			log.error( "(SchedulerRepositoryImpl.selectTasks) unexpected error occurred: {}", e.getMessage() );
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

		return tasks;
	}

	@Override
	public boolean insertTask( final Task task ) throws DatabaseException {
		return this.insertTask( task, false, null );
	}

	@Override
	public boolean insertTask( final Task task, final boolean picked, final String server ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.insertQuery( this.dbPlatform ) );
			preparedStatement.setString( 1, task.getName() );
			preparedStatement.setString( 2, task.getKey() );
			preparedStatement.setString( 3, task.getType().toString() );
			preparedStatement.setString( 4, task.getClazz().getName() );
			preparedStatement
					.setObject( 5, task.getData() != null ? mapper.writeValueAsString( task.getData() ) : null );
			preparedStatement
					.setString( 6, task.getData() != null ? task.getData().getClass().getName() : null );
			preparedStatement.setTimestamp( 7, Timestamp.from( task.getRunAt() ) );

			// if was a reccuring task set recurring configuation
			if ( task instanceof RecurringTask ) {
				Duration duration = ( (RecurringTask) task ).getRecurrence();
				preparedStatement.setObject( 8, duration.toString() );
			}
			else {
				preparedStatement.setObject( 8, null );
			}

			// set picked and picked_by if task already configured to run on
			// this server
			preparedStatement.setBoolean( 9, picked );
			preparedStatement.setString( 10, server );

			return preparedStatement.executeUpdate() == 1;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.insertTask) Can not insert task {}. Message error: {}", task.toString(),
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

	@Override
	public boolean updateTask( final String key, final Instant end ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.updateQuery( this.dbPlatform ) );
			preparedStatement.setTimestamp( 1, Timestamp.from( end ) );

			preparedStatement.setString( 2, key );

			return preparedStatement.executeUpdate() == 1;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.updateTask) Can not update task with key {}. Message error: {}", key,
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

	@Override
	public boolean updateRecurringTask( final RecurringTask task, final boolean picked, final String pickedBy )
			throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.updateRecurringTaskQuery( this.dbPlatform ) );
			preparedStatement.setTimestamp( 1, Timestamp.from( task.getRunAt() ) );
			preparedStatement.setBoolean( 2, picked );
			preparedStatement.setString( 3, pickedBy );

			preparedStatement.setString( 4, task.getKey() );

			return preparedStatement.executeUpdate() == 1;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.updateRecurringTask) Can not update task with key {}. Message error: {}",
					task.getKey(),
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

	@Override
	public boolean updateNotRunnedTask( final String server ) throws DatabaseException {
		if ( dataSource == null ) {
			throw new DatabaseException();
		}
		PreparedStatement preparedStatement = null;
		try ( Connection connection = dataSource.getConnection() ) {
			connection.setAutoCommit( true );

			preparedStatement = connection.prepareStatement( this.updateNotRunnedQuery( this.dbPlatform ) );
			preparedStatement.setString( 1, server );

			return preparedStatement.executeUpdate() == 1;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.updateNotRunnedTask) Can not update tasks not runned in server {}. Message error: {}",
					server,
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

	@SuppressWarnings( "unchecked" )
	private OneTimeTask buildOneTimeTaskFromResultSet( final ResultSet resultSet ) {
		try {
			OneTimeTask task = OneTimeTask.create( Class.forName( resultSet.getString( "task_class" ) ) )
					.name( resultSet.getString( "name" ) ).key( resultSet.getString( "key" ) )
					.runAt( resultSet.getTimestamp( "run_at" ).toInstant() );

			// process data of task
			final String data = resultSet.getString( "task_data" );
			final String dataClass = resultSet.getString( "task_data_class" );
			if ( data != null && !data.isEmpty() && dataClass != null && !dataClass.isEmpty() ) {
				Class< TaskData > dataClazz = (Class< TaskData >) Class.forName( dataClass );

				task.data( mapper.readValue( data, dataClazz ) ).dataClazz( dataClazz );
			}

			return task;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.buildOneTimeTaskFromResultSet) Can not serialize one time task from database. Error: {}",
					e.getMessage()
			);
		}
		return null;
	}

	@SuppressWarnings( "unchecked" )
	private RecurringTask buildRecurringTaskFromResultSet( final ResultSet resultSet ) {
		try {
			RecurringTask task = RecurringTask.create( Class.forName( resultSet.getString( "task_class" ) ) )
					.name( resultSet.getString( "name" ) ).key( resultSet.getString( "key" ) )
					.runAt( resultSet.getTimestamp( "run_at" ).toInstant() )
					.recurrence( Duration.parse( resultSet.getString( "recurrence" ) ) );

			// process data of task
			final String data = resultSet.getString( "task_data" );
			final String dataClass = resultSet.getString( "task_data_class" );
			if ( data != null && !data.isEmpty() && dataClass != null && !dataClass.isEmpty() ) {
				Class< TaskData > dataClazz = (Class< TaskData >) Class.forName( dataClass );

				task.data( mapper.readValue( data, dataClazz ) ).dataClazz( dataClazz );
			}

			return task;
		}
		catch ( Exception e ) {
			log.error(
					"(SchedulerRepositoryImpl.buildRecurringTaskFromResultSet) Can not serialize one time task from database. Error: {}",
					e.getMessage()
			);
		}
		return null;
	}

	private String updateRecurringTaskQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_UPDATE_RECURRING_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_UPDATE_RECURRING_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_UPDATE_RECURRING_QUERY;

		case DbUtils.DB_H2:
			return H2_UPDATE_RECURRING_QUERY;

		default:
			return POSTGRE_UPDATE_RECURRING_QUERY;
		}
	}

	private String updateNotRunnedQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_UPDATE_NOT_RUN_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_UPDATE_NOT_RUN_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_UPDATE_NOT_RUN_QUERY;

		case DbUtils.DB_H2:
			return H2_UPDATE_NOT_RUN_QUERY;

		default:
			return POSTGRE_UPDATE_NOT_RUN_QUERY;
		}
	}

	private String selectTasksForUpdateQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_SELECT_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_SELECT_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_SELECT_QUERY;

		case DbUtils.DB_H2:
			return H2_SELECT_QUERY;

		default:
			return POSTGRE_SELECT_QUERY;
		}
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

	private String updateQuery( final String platform ) {
		switch ( platform ) {
		case DbUtils.DB_MYSQL:
			return MYSQL_UPDATE_QUERY;

		case DbUtils.DB_MARIADB:
			return MYSQL_UPDATE_QUERY;

		case DbUtils.DB_MSSQL_SERVER:
			return MSSQL_UPDATE_QUERY;

		case DbUtils.DB_H2:
			return H2_UPDATE_QUERY;

		default:
			return POSTGRE_UPDATE_QUERY;
		}
	}
}
