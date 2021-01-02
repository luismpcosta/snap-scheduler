package io.opensw.scheduler.core.common;

import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.scheduler.task.TaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoidTask implements TaskExecutor {

	@Override
	public void execute() {
		log.debug( "Task run without data" );
	}

}
