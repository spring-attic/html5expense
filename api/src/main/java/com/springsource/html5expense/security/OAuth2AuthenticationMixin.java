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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.springsource.html5expense.security.OAuth2AuthenticationMixin.OAuth2AuthenticationDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = OAuth2AuthenticationDeserializer.class)
public class OAuth2AuthenticationMixin {

	public static class OAuth2AuthenticationDeserializer extends JsonDeserializer<OAuth2Authentication> {
		@Override
		public OAuth2Authentication deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			JsonNode tree = jp.readValueAsTree();
			ClientToken clientAuthentication = deserializeClientAuthentication(tree);
			UsernamePasswordAuthenticationToken userAuthentication = deserializeUserAuthentication(tree);
			OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
			oAuth2Authentication.setAuthenticated(true);
			return oAuth2Authentication;
		}

		private ClientToken deserializeClientAuthentication(JsonNode treeNode) {
			JsonNode clientAuthenticationNode = treeNode.get("clientAuthentication");
			String clientId = clientAuthenticationNode.get("clientId").getValueAsText();
			JsonNode resourceIdsNode = clientAuthenticationNode.get("resourceIds");
			Set<String> resourceIds = new HashSet<String>(resourceIdsNode.size());
			for (Iterator<JsonNode> resourceIdsIt = resourceIdsNode.getElements(); resourceIdsIt.hasNext();) {
				resourceIds.add(resourceIdsIt.next().getValueAsText());
			}
			String clientSecret = clientAuthenticationNode.get("clientSecret").getValueAsText();

			JsonNode scopeNode = clientAuthenticationNode.get("scope");
			Set<String> scope = new HashSet<String>(scopeNode.size());
			for (Iterator<JsonNode> scopeIt = scopeNode.getElements(); scopeIt.hasNext();) {
				scope.add(scopeIt.next().getValueAsText());
			}
			Set<GrantedAuthority> authorities = getAuthorities(clientAuthenticationNode);
			return new ClientToken(clientId, resourceIds, clientSecret, scope, authorities);

		}

		private UsernamePasswordAuthenticationToken deserializeUserAuthentication(JsonNode treeNode) {
			JsonNode userAuthenticationNode = treeNode.get("userAuthentication");
			String username = userAuthenticationNode.get("principal").get("username").getValueAsText();
			Set<GrantedAuthority> authorities = getAuthorities(userAuthenticationNode);
			UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
			return userAuthentication;
		}

		private Set<GrantedAuthority> getAuthorities(JsonNode parentNode) {
			JsonNode authoritiesNode = parentNode.get("authorities");
			Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(authoritiesNode.size());
			for (Iterator<JsonNode> authoritiesIt = authoritiesNode.getElements(); authoritiesIt.hasNext(); ) {
				JsonNode authorityNode = authoritiesIt.next();
				authorities.add(new SimpleGrantedAuthority(authorityNode.get("authority").getValueAsText()));
			}
			return authorities;
		}
		
	}
}
