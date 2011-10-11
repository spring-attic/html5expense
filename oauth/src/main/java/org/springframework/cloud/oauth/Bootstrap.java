package org.springframework.cloud.oauth;

public class Bootstrap {
	public void go() {
		String vcapServices = System.getenv("VCAP_SERVICES");
		System.out.println("VCAP SERVICES:  " + vcapServices);		
	}
}
