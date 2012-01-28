package com.springsource.html5expense.web;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * 
 * {@link ApplicationContextInitializer application context initializers} are Spring callback interfaces that let you
 *
 * 
 * 
 * @author Josh Long
 */
public class CloudAwareApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {
    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        CloudEnvironment cloudEnvironment = new CloudEnvironment();
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String profile = cloudEnvironment.isCloudFoundry() ? "cloud": "local" ;
        environment.setActiveProfiles(profile);        
    }
}
