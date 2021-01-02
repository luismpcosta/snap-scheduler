package io.opensw.scheduler.core.scheduler.task;

import java.time.Duration;
import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode( callSuper = true )
public class RecurringTask extends Task {

	protected static final String DEFAULT_NAME = "Reccuring task";
	
	private Duration recurrence;

	/**
	 * Recurring task default constructor
	 * 
	 * @param clazz task class
	 */
	public RecurringTask( final Class< ? > clazz ) {
		super( clazz, DEFAULT_NAME, TaskType.RECURRING );
	}

	/**
	 * Recurring task default constructor
	 * 
	 * @param clazz      task class
	 * @param recurrence task recurrence time
	 */
	public RecurringTask( final Class< ? > clazz, final Duration recurrence ) {
		super( clazz, DEFAULT_NAME, TaskType.RECURRING );
		this.recurrence = recurrence;
	}

	/**
	 * Recurring task constructor with required fields (key)
	 * 
	 * @param clazz task class
	 * @param name  of task
	 */
	public RecurringTask( final Class< ? > clazz, final String name ) {
		super( clazz, name, TaskType.RECURRING );
	}

	/**
	 * Recurring task constructor with key and data fields
	 * 
	 * @param clazz task class
	 * @param key   is the unique identifier of task (usually use
	 *              UUID.randomUUID().toString())
	 * @param data  is the data needed to run task
	 */
	public RecurringTask( final Class< ? > clazz, final String key, final TaskData data ) {
		super( clazz, DEFAULT_NAME, key, TaskType.RECURRING, data );
	}

	/**
	 * Recurring task constructor with required fields (key)
	 * 
	 * @param clazz task class
	 * @param name  of task
	 * @param key   is the unique identifier of task (usually use
	 *              UUID.randomUUID().toString())
	 */
	public RecurringTask( final Class< ? > clazz, final String name, final String key ) {
		super( clazz, name, key, TaskType.RECURRING );
	}

	/**
	 * Recurring task constructor with key and data fields
	 * 
	 * @param clazz task class
	 * @param name  of task
	 * @param key   is the unique identifier of task (usually use
	 *              UUID.randomUUID().toString())
	 * @param data  is the data needed to run task
	 */
	public RecurringTask( final Class< ? > clazz, final String name, final String key, final TaskData data ) {
		super( clazz, name, key, TaskType.RECURRING, data );
	}

	/**
	 * Create instance
	 * 
	 * @param clazz with execution implementation
	 * @return this instance of object
	 */
	public static RecurringTask create( final Class< ? > clazz ) {
		return new RecurringTask( clazz );
	}

	/**
	 * Set recurrence
	 * 
	 * @param recurrence value to be set
	 * @return this instance of object
	 */
	public RecurringTask recurrence( final Duration recurrence ) {
		this.recurrence = recurrence;

		return this;
	}

	/**
	 * Set key
	 * 
	 * @param key value to set
	 * @return this instance
	 */
	@Override
	public RecurringTask key( final String key ) {
		this.setKey( key );

		return this;
	}

	/**
	 * Set name
	 * 
	 * @param name value to set
	 * @return this instance
	 */
	@Override
	public RecurringTask name( final String name ) {
		this.setName( name );

		return this;
	}

	/**
	 * Set run at
	 * 
	 * @param runAt value to set
	 * @return this instance
	 */
	@Override
	public RecurringTask runAt( final Instant runAt ) {
		this.setRunAt( runAt );

		return this;
	}

	/**
	 * Set data of task
	 * 
	 * @param data value to set
	 * @return this instance
	 */
	public RecurringTask data( final TaskData data ) {
		if ( data == null ) {
			return this;
		}

		this.setData( data );
		this.setDataClazz( data.getClass() );

		return this;
	}

	/**
	 * Set data class
	 * 
	 * @param clazz value to set
	 * @return this instance
	 */
	public RecurringTask dataClazz( final Class< ? > clazz ) {
		if ( clazz == null ) {
			return this;
		}

		this.setDataClazz( clazz );

		return this;
	}

	/**
	 * Set type
	 * 
	 * @param type value to set
	 * @return this instance
	 */
	@Override
	public RecurringTask type( final TaskType type ) {
		this.setType( type );

		return this;
	}

}
