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
package com.springsource.oauthservice;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.RandomValueTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Identity controller, allowing a client to fetch the identify of the authenticated user.
 * Intended to be secured by OAuth2, so that it will only fetch the identity when given a valid access token.
 * @author wallsc
 */
@Controller
public class IdentityController {
	
	private final AuthorizationServerTokenServices tokenServices;

	@Inject
	public IdentityController(AuthorizationServerTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}

	@RequestMapping(value="/me", method=RequestMethod.GET)
	public @ResponseBody Map<String, String> getIdentity(Principal principal) {
		HashMap<String, String> identityMap = new HashMap<String, String>();
		identityMap.put("id", principal.getName());
		return identityMap;
	}

	@RequestMapping(value="/me/authentication", method=RequestMethod.GET)
	public @ResponseBody OAuth2Authentication getIdentity(NativeWebRequest request) {
		// TODO: Naively pull the access token from the header for now...if this works, refine this
		String authHeader = request.getHeader("Authorization");
		String accessToken = authHeader.split("\\s")[1];
		// TODO - unlike  OAuth2ProviderTokenServices, AuthorizationServerTokenServices does not have a loadAuthentication method 
		// - what should be used here instead?
		return ((RandomValueTokenServices)tokenServices).loadAuthentication(accessToken);
	}

}
