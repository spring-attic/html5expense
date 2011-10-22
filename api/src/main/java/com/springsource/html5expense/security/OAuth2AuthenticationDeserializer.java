package com.springsource.html5expense.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizedClientAuthenticationToken;
import org.springframework.security.oauth2.provider.ClientAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class OAuth2AuthenticationDeserializer extends JsonDeserializer<OAuth2Authentication> {

	@Override
	public OAuth2Authentication deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode tree = jp.readValueAsTree();
		
		// 1. deserialize a ClientAuthenticationToken
		JsonNode clientAuthenticationNode = tree.get("clientAuthentication");
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
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		ClientAuthenticationToken clientAuthentication = new AuthorizedClientAuthenticationToken(clientId, resourceIds, clientSecret, scope, authorities);
		
		// 2. deserialize an Authorization
		UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken("craig", null, authorities);

		// 3. create and return an OAuth2Authentication
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(clientAuthentication, userAuthentication);
		oAuth2Authentication.setAuthenticated(true);
		return oAuth2Authentication;
	}

}
