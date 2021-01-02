package io.opensw.scheduler.core.scheduler.task;

import java.time.Instant;

public class OneTimeTask extends Task {

	protected static final String DEFAULT_NAME = "One time task";
	
	/**
	 * One time task default constructor
	 *
	 * @param clazz task class
	 */
	public OneTimeTask( final Class< ? > clazz ) {
		super( clazz, DEFAULT_NAME, TaskType.ONE_TIME );
	}

	/**
	 * One time task constructor with required fields (key)
	 * 
	 * @param clazz task class
	 * @param name of task
	 */
	public OneTimeTask( final Class< ? > clazz, final String name ) {
		super( clazz, name, TaskType.ONE_TIME );
	}

	/**
	 * One time task constructor with key and data fields
	 * 
	 * @param clazz task class
	 * @param key  is the unique identifier of task (usually use
	 *             UUID.randomUUID().toString())
	 * @param data is the data needed to run task
	 */
	public OneTimeTask( final Class< ? > clazz, final String key, final TaskData data ) {
		super( clazz, DEFAULT_NAME, key, TaskType.ONE_TIME, data );
	}

	/**
	 * One time task constructor with required fields (key)
	 * 
	 * @param clazz task class
	 * @param name of task
	 * @param key  is the unique identifier of task (usually use
	 *             UUID.randomUUID().toString())
	 */
	public OneTimeTask( final Class< ? > clazz, final String name, final String key ) {
		super( clazz, name, key, TaskType.ONE_TIME );
	}

	/**
	 * One time task constructor with key and data fields
	 * 
	 * @param clazz task class
	 * @param name of task
	 * @param key  is the unique identifier of task (usually use
	 *             UUID.randomUUID().toString())
	 * @param data is the data needed to run task
	 */
	public OneTimeTask( final Class< ? > clazz, final String name, final String key, final TaskData data ) {
		super( clazz, name, key, TaskType.ONE_TIME, data );
	}

	/**
	 * Create instance
	 * 
	 * @param clazz with execution implementation
	 * @return this instance of object
	 */
	public static OneTimeTask create( final Class< ? > clazz ) {
		return new OneTimeTask( clazz );
	}
	
	/**
	 * Set key
	 * 
	 * @param key value to set
	 * @return this instance
	 */
	@Override
	public OneTimeTask key( final String key) {
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
	public OneTimeTask name( final String name ) {
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
	public OneTimeTask runAt( final Instant runAt ) {
		this.setRunAt( runAt );

		return this;
	}

	/**
	 * Set data of task
	 * 
	 * @param data value to set
	 * @return this instance
	 */
	public OneTimeTask data( final TaskData data ) {
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
	public OneTimeTask dataClazz( final Class< ? > clazz) {
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
	public OneTimeTask type( final TaskType type ) {
		this.setType( type );

		return this;
	}
}
