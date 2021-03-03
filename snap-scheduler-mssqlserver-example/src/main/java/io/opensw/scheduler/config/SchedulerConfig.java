package io.opensw.scheduler.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.zaxxer.hikari.HikariDataSource;

import io.opensw.scheduler.core.config.SnapAppConfig;

/**
 * The Class SchedulerConfig.
 */
@Configuration
@EnableScheduling
@Import( SnapAppConfig.class )
public class SchedulerConfig {

	private static final String SNAP_DB_POOL_NAME = "snap-pool";

	/**
	 * Configure a custom dataSource.
	 * 
	 * @param properties the properties
	 * @return the dataSource 
	 */
	@Bean( name = "snapDataSource" )
	public DataSource customDataSource( final DataSourceProperties properties ) {

		final HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type( HikariDataSource.class ).build();

		dataSource.setPoolName( SNAP_DB_POOL_NAME );
		
		return dataSource;
	}

}
