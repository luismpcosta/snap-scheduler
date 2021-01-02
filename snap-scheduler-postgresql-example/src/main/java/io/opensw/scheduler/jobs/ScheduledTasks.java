package io.opensw.scheduler.jobs;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.annotations.SnapLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledTasks {

	@Scheduled(fixedRate = 30000)
	@SnapLock(key = "REPORT_CURRENT_TIME", time = 60)
	public void reportCurrentTime() {
		log.debug("The time is now {}", Instant.now());
		try {
			Thread.sleep( 10000 );
		}
		catch ( Exception e ) {
			// TODO: handle exception
		}
	}

}
