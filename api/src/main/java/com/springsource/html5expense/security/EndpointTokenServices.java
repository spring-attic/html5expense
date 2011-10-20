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
package com.springsource.html5expense.security;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenDetails;
import org.springframework.security.oauth2.provider.token.OAuth2ProviderTokenServices;
import org.springframework.web.client.RestTemplate;

public class EndpointTokenServices implements OAuth2ProviderTokenServices {

	private static final String DEFAULT_AUTHORIZATION_ENDPOINT = "https://haboauth.cloudfoundry.com/me/authorization"; 
	
	private String authorizationEndpoint = DEFAULT_AUTHORIZATION_ENDPOINT;
	
	private RestTemplate restTemplate = new RestTemplate();

	public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
		// Fetch an authentication from the OAuth server using the access token
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer "+ accessToken);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(authorizationEndpoint, HttpMethod.GET, requestEntity, String.class);
		
		return null;
	}

	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
		throw new UnsupportedOperationException("Can't create an access token through this implementation.");
	}

	public OAuth2AccessToken refreshAccessToken(RefreshTokenDetails refreshToken) throws AuthenticationException {
		throw new UnsupportedOperationException("Can't refresh an access token through this implementation.");
	}

}
