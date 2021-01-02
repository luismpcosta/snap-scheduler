package io.opensw.scheduler.core.annotations;

import java.time.Instant;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.domain.lock.LockRepository;
import io.opensw.scheduler.core.events.obj.TaskRunEvent;
import io.opensw.scheduler.core.scheduler.task.LockedTask;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class AuditAspect.
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
@Component
public class SnapLockAspect {

	private final LockRepository lockRepository;

	private final ApplicationEventPublisher applicationEventPublisher;

	private static final String SERVER_NAME = ServerUtils.loadServerName();

	/**
	 * Intercepts all methods annotated with @{@link SnapLock}.
	 */
	@Pointcut( "execution(@io.opensw.scheduler.core.annotations.SnapLock * *(..))" )
	public void snapLock() {
		// method without body
	}

	/**
	 * PreAuthorizeApp Resolver.
	 *
	 * @param point    to lock
	 * @param snapLock annotation lock data
	 * @throws Throwable exception
	 * @return method execution
	 */
	@Around( "snapLock() && @annotation(snapLock)" )
	public Object snapLockResolver( final ProceedingJoinPoint point, final SnapLock snapLock ) throws Throwable {
		log.trace( "Intercepting invocation of method {}", point.getSignature().getName() );

		String method = point.getSignature().getDeclaringTypeName().concat( "." )
				.concat( point.getSignature().getName() );
		boolean lock = lockRepository.lockTask( snapLock.key(), method, snapLock.time(), SERVER_NAME );
		if ( lock ) {
			Exception exception = null;
			final Instant start = Instant.now();
			try {
				return point.proceed();
			}
			catch ( Exception e ) {
				exception = e;
				log.error( "(SnapLockAspect.snapLockResolver) Error occured on running task {}.", snapLock.key() );
			}
			finally {
				final LockedTask task = LockedTask.create( Class.forName( point.getSignature().getDeclaringTypeName() ) ).key( snapLock.key() );
				applicationEventPublisher.publishEvent(
						TaskRunEvent.create( task, SERVER_NAME, start, Instant.now(), exception )
				);
			}
		}

		return Optional.empty();
	}

}
