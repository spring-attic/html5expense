package com.springsource.html5expense.security;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class EndpointTokenModule extends SimpleModule {
	
	public EndpointTokenModule() {
		super("EndpointTokenModule", new Version(1, 0, 0, null));
	}
	
	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(OAuth2Authentication.class, OAuth2AuthenticationMixin.class);
	}
	
}
