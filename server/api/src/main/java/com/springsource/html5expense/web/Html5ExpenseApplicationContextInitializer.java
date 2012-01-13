package com.springsource.html5expense.web;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * 
 * A simple {@link ApplicationContextInitializer context initializer}.
 * 
 * @author Josh Long
 */
public class Html5ExpenseApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {
    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        CloudEnvironment cloudEnvironment = new CloudEnvironment();
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String profile = cloudEnvironment.isCloudFoundry() ? "cloud": "local" ;
        environment.setActiveProfiles(profile);        
    }
}
