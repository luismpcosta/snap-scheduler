package io.opensw.scheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties( prefix = "snap.scheduler" )
public class SnapSchedulerProperties {

	/**
	 * snap scheduler enabled status
	 */
	private boolean enabled = true;

	/** database polling tasks interval */
	@DurationUnit( ChronoUnit.MINUTES )
	private Duration dbPollingInterval;

	/**
	 * Database polling tasks interval
	 * 
	 * @return polling interval in milliseconds
	 */
	public long dbPollingInterval() {
		if ( dbPollingInterval == null || dbPollingInterval.isZero() || dbPollingInterval.isNegative() ) {
			return 300000;
		}
		return dbPollingInterval.toMillis();
	}
	
}
