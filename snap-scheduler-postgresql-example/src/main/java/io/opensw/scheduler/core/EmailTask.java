package io.opensw.scheduler.core;

import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.scheduler.task.TaskDataExecutor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailTask extends TaskDataExecutor< Email > {

	private final TestService testService;

	@Override
	public void execute( Email data ) {
		testService.test();
	}

}
