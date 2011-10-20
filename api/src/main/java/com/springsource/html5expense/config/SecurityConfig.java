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

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.provider.token.JdbcOAuth2ProviderTokenServices;
import org.springframework.security.oauth2.provider.token.OAuth2ProviderTokenServices;

@Configuration
@ImportResource("classpath:com/springsource/html5expense/config/security.xml")
public class SecurityConfig {

	@Inject
	@Named("tokenDataSource")
	private DataSource dataSource;
	
	@Bean
	public OAuth2ProviderTokenServices tokenServices() {
		// TODO: Perhaps this should be handled via the OAuth service and not via a shared DB
		JdbcOAuth2ProviderTokenServices tokenServices = new JdbcOAuth2ProviderTokenServices(dataSource);
		tokenServices.setSupportRefreshToken(true);
		return tokenServices;
	}
	
	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}
	
}
