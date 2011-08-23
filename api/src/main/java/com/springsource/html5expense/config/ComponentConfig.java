package com.springsource.html5expense.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application @Components such as @Services, @Repositories, and @Controllers.
 * Loads externalized property values required to configure the various application properties.
 * Not much else here, as we rely on @Component scanning in conjunction with @Inject by-type autowiring.
 * @author Keith Donald
 */
@Configuration
@ComponentScan(basePackages="com.springsource.html5expense", excludeFilters={ @Filter(Configuration.class)} )
public class ComponentConfig {

}