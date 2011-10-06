package org.springframework.cloud.oauth;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

// shouldn't need this if CloudFoundry would set the "cloud" profile as active automatically
public class CloudApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	
	public void initialize(ConfigurableApplicationContext applicationContext) {		
		CloudEnvironment env = new CloudEnvironment();
		if (env.getInstanceInfo() != null) {
			applicationContext.getEnvironment().setActiveProfiles("cloud");
		} else {
			applicationContext.getEnvironment().setActiveProfiles("dev");
		}
	}

}
