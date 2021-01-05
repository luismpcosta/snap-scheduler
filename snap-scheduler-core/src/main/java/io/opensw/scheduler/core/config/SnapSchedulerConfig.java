package io.opensw.scheduler.core.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import io.opensw.scheduler.SnapSchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@ComponentScan( "io.opensw.scheduler" )
@RequiredArgsConstructor
@Slf4j
public class SnapSchedulerConfig {

	private final SnapSchedulerProperties properties;

	private final DataSourceProperties dataSourceProperties;

	/**
	 * Bean to specific datasource
	 * 
	 * @return datasource
	 */
	@Bean( name = "snapDataSource" )
	public DataSource snapDataSource() {
		if ( properties.getDatasource() != null ) {
			log.info( "Configure custom datasource." );

			DataSourceBuilder< ? > dataSourceBuilder = DataSourceBuilder.create();
			dataSourceBuilder.driverClassName( properties.getDatasource().getDriverClassName() );
			dataSourceBuilder.url( properties.getDatasource().getUrl() );
			dataSourceBuilder.username( properties.getDatasource().getUsername() );
			dataSourceBuilder.password( properties.getDatasource().getPassword() );

			return dataSourceBuilder.build();
		}

		log.info( "Reuse existing datasource configuration." );
		return this.dataSourceProperties.initializeDataSourceBuilder().build();
	}

}
