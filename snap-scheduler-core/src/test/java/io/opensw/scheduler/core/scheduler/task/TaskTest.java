package io.opensw.scheduler.core.scheduler.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.opensw.scheduler.core.common.Email;
import io.opensw.scheduler.core.common.VoidTask;

@RunWith( JUnitPlatform.class )
class TaskTest {

	@Test
	void oneTimeTask() {
		OneTimeTask task = new OneTimeTask( VoidTask.class );
		assertEquals( VoidTask.class, task.getClazz() );

		task = new OneTimeTask( VoidTask.class, "Test name" );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );

		final String key = UUID.randomUUID().toString();

		task = new OneTimeTask( VoidTask.class, "Test name", key );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );
		assertEquals( key, task.getKey() );

		task = new OneTimeTask( VoidTask.class, key, new Email() );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( OneTimeTask.DEFAULT_NAME, task.getName() );
		assertEquals( key, task.getKey() );
		assertNotNull( task.getData() );

		task = new OneTimeTask( VoidTask.class, "Test name", key, new Email() );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );
		assertEquals( key, task.getKey() );
		assertNotNull( task.getData() );
	}

	@Test
	void recurringTask() {
		RecurringTask task = new RecurringTask( VoidTask.class );
		assertEquals( VoidTask.class, task.getClazz() );

		task = new RecurringTask( VoidTask.class, "Test name" );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );

		final String key = UUID.randomUUID().toString();

		task = new RecurringTask( VoidTask.class, "Test name", key );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );
		assertEquals( key, task.getKey() );

		task = new RecurringTask( VoidTask.class, key, new Email() );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( RecurringTask.DEFAULT_NAME, task.getName() );
		assertEquals( key, task.getKey() );
		assertNotNull( task.getData() );

		task = new RecurringTask( VoidTask.class, "Test name", key, new Email() );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Test name", task.getName() );
		assertEquals( key, task.getKey() );
		assertNotNull( task.getData() );

		task = new RecurringTask( VoidTask.class, Duration.ofHours( 1 ) );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( RecurringTask.DEFAULT_NAME, task.getName() );
		assertNotNull( task.getRecurrence() );
	}
	
	@Test
	void lockedTask() {
		LockedTask task = LockedTask.create( VoidTask.class );
		assertEquals( VoidTask.class, task.getClazz() );
		assertEquals( "Locked task", task.getName() );

		final Email email = new Email();
		email.setEmail( "test@opensw.io" );
		task.data( email );
		assertEquals( email, task.getData() );
		assertEquals( Email.class, task.getDataClazz() );
		
		final String key = UUID.randomUUID().toString();		
		task.key( key );
		assertEquals( key, task.getKey() );

		final String name = "Task name";
		task.name( name );
		assertEquals( name, task.getName() );
		
		task.type( TaskType.LOCKED_TASK );
		assertEquals( TaskType.LOCKED_TASK, task.getType() );
		
		final Instant now = Instant.now();
		task.runAt( now );
		assertEquals( now, task.getRunAt() );
		
		// null do not update data
		task.data( null );
		assertNotNull( task.getData() );

		// null do not update data class 
		task.dataClazz( null );
		assertNotNull( task.getDataClazz() );
		
	}

}
