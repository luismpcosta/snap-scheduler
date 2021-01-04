package io.opensw.scheduler.core.scheduler.task;

import java.time.Instant;

public class LockedTask extends Task {

	/**
	 * Locked task default constructor
	 *
	 * @param clazz task class
	 */
	public LockedTask( final Class< ? > clazz ) {
		super( clazz, "Locked task", TaskType.LOCKED_TASK);
	}

	/**
	 * Create instance
	 * 
	 * @param clazz with execution implementation
	 * @return this instance of object
	 */
	public static LockedTask create( final Class< ? > clazz ) {
		return new LockedTask( clazz );
	}
	
	/**
	 * Set key
	 * 
	 * @param key value to set
	 * @return this instance
	 */
	@Override
	public LockedTask key( final String key) {
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
	public LockedTask name( final String name ) {
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
	public LockedTask runAt( final Instant runAt ) {
		this.setRunAt( runAt );

		return this;
	}

	/**
	 * Set data of task
	 * 
	 * @param data value to set
	 * @return this instance
	 */
	public LockedTask data( final TaskData data ) {
		if ( data == null ) {
			return this;
		}
		
		this.setData( data );
		this.dataClazz( data.getClass() );

		return this;
	}
	
	/**
	 * Set data class
	 * 
	 * @param clazz value to set
	 * @return this instance
	 */
	public LockedTask dataClazz( final Class< ? > clazz) {
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
	public LockedTask type( final TaskType type ) {
		this.setType( type );

		return this;
	}
}
