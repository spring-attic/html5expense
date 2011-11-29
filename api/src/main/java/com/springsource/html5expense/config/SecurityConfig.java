/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import com.springsource.html5expense.security.EndpointTokenServices;

@Configuration
@ImportResource("classpath:com/springsource/html5expense/config/security.xml")
public class SecurityConfig {
	
	@Bean
	public AuthenticationEntryPoint entryPoint() {
		return new Http403ForbiddenEntryPoint();
	}

	// OAuth beans
	@Bean
	public AuthorizationServerTokenServices tokenServices() {
		// TODO: Pull the authentication endpoint URL from the environment
		//       Or, if the oauth service becomes a "native" CF service, then this whole bean could be consumed as a CF service.
		return new EndpointTokenServices("https://haboauth.cloudfoundry.com/me/authentication");
	}
	
}
