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

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenDetails;
import org.springframework.security.oauth2.provider.token.OAuth2ProviderTokenServices;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of OAuth2ProviderTokenServices that loads authentication via the OAuth service's authentication endpoint.
 * @author wallsc
 */
public class EndpointTokenServices implements OAuth2ProviderTokenServices {

	private final String oauthAuthenticationUrl;
	
	private final RestTemplate restTemplate;
	
	public EndpointTokenServices(String oauthAuthenticationUrl) {
		this.oauthAuthenticationUrl = oauthAuthenticationUrl;
		
		this.restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
		for (HttpMessageConverter<?> httpMessageConverter : messageConverters) {
			if (httpMessageConverter.getClass().equals(MappingJacksonHttpMessageConverter.class)) {
				MappingJacksonHttpMessageConverter jsonConverter = (MappingJacksonHttpMessageConverter) httpMessageConverter;
				ObjectMapper mapper = new ObjectMapper();
				mapper.getDeserializationConfig().addMixInAnnotations(OAuth2Authentication.class, OAuth2AuthenticationMixin.class);
				jsonConverter.setObjectMapper(mapper);
			}
		}
	}
	

	public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
		// TODO: Probably should catch REST client exceptions and rethrow as AuthenticationException
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer "+ accessToken);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		ResponseEntity<OAuth2Authentication> response = restTemplate.exchange(oauthAuthenticationUrl, HttpMethod.GET, requestEntity, OAuth2Authentication.class);
		
		OAuth2Authentication oauth2Authentication = response.getBody();
		return oauth2Authentication;
	}

	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
		throw new UnsupportedOperationException("Can't create an access token through this implementation.");
	}

	public OAuth2AccessToken refreshAccessToken(RefreshTokenDetails refreshToken) throws AuthenticationException {
		throw new UnsupportedOperationException("Can't refresh an access token through this implementation.");
	}

}
