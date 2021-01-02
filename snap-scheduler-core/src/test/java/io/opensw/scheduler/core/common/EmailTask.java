package io.opensw.scheduler.core.common;

import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.scheduler.task.TaskDataExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTask extends TaskDataExecutor< Email > {

	@Override
	public void execute( Email data ) {
		log.debug( "Task run with data: {}", data );
	}

}
