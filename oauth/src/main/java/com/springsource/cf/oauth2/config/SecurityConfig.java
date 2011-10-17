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
package com.springsource.cf.oauth2.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.JdbcOAuth2ProviderTokenServices;

@Configuration
@ImportResource({"classpath:com/springsource/cf/oauth2/config/security.xml", "classpath:com/springsource/cf/oauth2/config/oauth.xml"})
public class SecurityConfig {
	
	@Inject
	private DataSource dataSource;
	
	@Bean
	public ClientDetailsService clientDetails() {
		JdbcClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
		clientDetailsService.setSelectClientDetailsSql("select apiKey, resourceIds, secret, scope, "
				+ "grantTypes, redirectUrl, authorities from App where apiKey = ?");
		return clientDetailsService;
	}
	
	@Bean 
	public JdbcOAuth2ProviderTokenServices tokenServices() {
		JdbcOAuth2ProviderTokenServices tokenServices = new JdbcOAuth2ProviderTokenServices(dataSource);
		tokenServices.setSupportRefreshToken(true);
		return tokenServices;
	}
	
	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}

}
