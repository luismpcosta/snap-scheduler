package io.opensw.scheduler.core.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import io.opensw.scheduler.core.annotations.SnapLock;
import io.opensw.scheduler.core.annotations.SnapLockAspect;
import io.opensw.scheduler.core.domain.lock.LockRepository;
import io.opensw.scheduler.core.events.obj.TaskRunEvent;

@RunWith( JUnitPlatform.class )
class SnapLockAspectTest {

	@Mock
	private LockRepository lockRepository;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private SnapLockAspect snapLockAspect;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks( this );

		snapLockAspect = new SnapLockAspect( lockRepository, applicationEventPublisher );
	}

	@Test
	void aspect() throws Throwable {
		final ProceedingJoinPoint point = Mockito.mock( ProceedingJoinPoint.class );

		final String key = UUID.randomUUID().toString();
		final SnapLock snapLock = Mockito.mock( SnapLock.class );
		when( snapLock.key() ).thenReturn( key );

		final Signature signature = Mockito.mock( Signature.class );
		when( signature.getName() ).thenReturn( "test" );
		when( signature.getDeclaringTypeName() ).thenReturn( "io.opensw.scheduler.core.scheduler.task.LockedTask" );
		when( point.getSignature() ).thenReturn( signature );

		when(
				lockRepository
						.lockTask(
								Mockito.eq( key ), Mockito.eq( "io.opensw.scheduler.core.scheduler.task.LockedTask.test" ), Mockito.anyLong(),
								Mockito.eq( "DESKTOP-GIFRML4" )
						)
		).thenReturn( false, true );

		snapLockAspect.snapLockResolver( point, snapLock );

		snapLockAspect.snapLockResolver( point, snapLock );

		verify( applicationEventPublisher, times( 1 ) ).publishEvent( Mockito.any( TaskRunEvent.class ) );
	}

}
