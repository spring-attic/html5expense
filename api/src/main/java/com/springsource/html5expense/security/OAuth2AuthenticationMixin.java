package com.springsource.html5expense.security;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = OAuth2AuthenticationDeserializer.class)
public class OAuth2AuthenticationMixin {	
}
