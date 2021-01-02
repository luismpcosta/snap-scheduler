package io.opensw.scheduler.core.scheduler.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
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
}
