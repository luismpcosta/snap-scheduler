package io.opensw.scheduler.core.scheduler.task;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class Task {

	private final Class<?> clazz;

	private TaskType type;

	private String name;

	private String key;

	private Instant runAt;

	private TaskData data;

	private Class< ? > dataClazz;

	/**
	 * Task constructor
	 * 
	 * @param clazz task class
	 * @param name of task
	 * @param type of task
	 */
	public Task( final Class<?> clazz, final String name, final TaskType type ) {
		this.clazz = clazz;
		this.name = name;
		this.type = type;
		this.key = UUID.randomUUID().toString();
	}

	/**
	 * Task constructor
	 * 
	 * @param clazz task class
	 * @param name of task
	 * @param key  identifier of task
	 * @param type of task
	 * @param data to run task
	 */
	public Task( final Class<?> clazz, final String name, final String key, final TaskType type, final TaskData data ) {
		this.clazz = clazz;
		this.name = name;
		this.key = key;
		this.type = type;
		this.data = data;
	}
	
	/**
	 * Task constructor
	 * 
	 * @param clazz task class
	 * @param name of task
	 * @param key  identifier of task
	 * @param type of task
	 */
	public Task( final Class<?> clazz, final String name, final String key, final TaskType type ) {
		this.clazz = clazz;
		this.name = name;
		this.key = key;
		this.type = type;
	}

	/**
	 * Set type
	 * 
	 * @param type value to set
	 * @return this instance
	 */
	public abstract Task type( final TaskType type );

	/**
	 * Set key
	 * 
	 * @param key value to set
	 * @return this instance
	 */
	public abstract Task key( final String key );
	
	/**
	 * Set name
	 * 
	 * @param name value to set
	 * @return this instance
	 */
	public abstract Task name( final String name );

	/**
	 * Set run at
	 * 
	 * @param runAt value to set
	 * @return this instance
	 */
	public abstract Task runAt( final Instant runAt );

	/**
	 * Set data of task
	 * 
	 * @param data value to set
	 * @return this instance
	 */
	public abstract Task data( final TaskData data );
	
	/**
	 * Set data class
	 * 
	 * @param clazz value to set
	 * @return this instance
	 */
	public abstract Task dataClazz( final Class< ? > clazz);
}
