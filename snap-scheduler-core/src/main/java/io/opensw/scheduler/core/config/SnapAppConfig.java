package io.opensw.scheduler.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan( "io.opensw.scheduler" )
public class SnapAppConfig {

}
