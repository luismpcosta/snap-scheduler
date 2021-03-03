package io.opensw.scheduler.config;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
	 * @param schema the schema
	 * @return the dataSource 
	 */
	@Bean( name = "snapDataSource" )
	public DataSource customDataSource( final DataSourceProperties properties,
			@Value( "${spring.datasource.hikari.schema}" ) final String schema ) {

		final HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type( HikariDataSource.class ).build();

		dataSource.setPoolName( SNAP_DB_POOL_NAME );
		
		if ( !StringUtils.isEmpty( schema ) ) {
			dataSource.setSchema( schema );
		}

		return dataSource;
	}

}
