package com.springsource.html5expense.web;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * 
 * {@link ApplicationContextInitializer application context initializers} are Spring callback interfaces that let you
 * tailor the Spring application context before it's run. This is useful if you want to register custom classes to be run, 
 * or conditionally set the <EM>active profile</EM> or anything else.  
 * 
 * Here, we're using this callback, which runs <EM>right</EM> before the {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet} machinery 
 * instantiates the Spring {@link org.springframework.context.ApplicationContext application context}, to detect whether
 * the application's running inside of Cloud Foundry (by asking the {@link org.cloudfoundry.runtime.env.CloudEnvironment#isCloudFoundry() cloud foundry API}) and
 * set the active profile accordingly.
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
