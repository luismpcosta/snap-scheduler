package io.opensw.scheduler.interfaces;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opensw.scheduler.core.Email;
import io.opensw.scheduler.core.EmailTask;
import io.opensw.scheduler.core.exceptions.TaskDefinitionException;
import io.opensw.scheduler.core.exceptions.UnexpectedException;
import io.opensw.scheduler.core.scheduler.SnapScheduler;
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;
import io.opensw.scheduler.core.scheduler.task.RecurringTask;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SchedulerEndpoints {

	private final SnapScheduler snapScheduler;

	@PostMapping( "/scheduler" )
	public ResponseEntity< Void > createTask() throws TaskDefinitionException, UnexpectedException {
		Email email = new Email();
		email.setEmail( "one.teste@gmail.com" );

		RecurringTask recurringTask = RecurringTask.create( EmailTask.class ).runAt( Instant.now().plus( 1, ChronoUnit.MINUTES ) )
				.name( "name teste" ).key( "KEY_".concat( UUID.randomUUID().toString() ) )
				.recurrence( Duration.ofMinutes( 10 ) ).data( email );

		// call schedule to schedule task and save them in database
		snapScheduler.schedule( recurringTask );

		
		OneTimeTask oneTimeTask = OneTimeTask.create( EmailTask.class ).runAt( Instant.now().plus( 1, ChronoUnit.MINUTES ) )
				.name( "One Teste" ).key( "O_".concat( UUID.randomUUID().toString() ) )
				.data( email );
		
		// call schedule to schedule task and save them in database
		snapScheduler.schedule( oneTimeTask );
		
		return ResponseEntity.ok().build();
	}

}
