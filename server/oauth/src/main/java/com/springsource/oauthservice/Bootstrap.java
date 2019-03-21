/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.oauthservice;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.org.codehaus.jackson.JsonFactory;
import org.cloudfoundry.org.codehaus.jackson.JsonParser;
import org.cloudfoundry.org.codehaus.jackson.map.ObjectMapper;

/**
 * TEMPORARY: Bootstrap to log the value of VCAP_SERVICES so that I can create a DB URL to the bound database service.
 *
 * @author wallsc
 */
public class Bootstrap {
	private static final Log LOG = LogFactory.getLog(Bootstrap.class);

	public void go() throws Exception {
		String vcapServices = System.getenv("VCAP_SERVICES");
		LOG.debug("VCAP_SERVICES: " + vcapServices);
		// jdbc:postgresql://172.30.48.126:5432/d6f69ba9c3c6349ac830af2973e31b779

		// pull values out and construct JDBC URL
		Map credentials = getCredentialsMap(vcapServices);
		String dbName = (String) credentials.get("name");
		String host = (String) credentials.get("host");
		Integer port = (Integer) credentials.get("port");
		String username = (String) credentials.get("username");
		String password = (String) credentials.get("password");

		LOG.debug("    JDBC URL:  jdbc:postgresql://" + host + ":" + port + "/" + dbName);
		LOG.debug("    DB USERNAME: " + username);
		LOG.debug("    DB PASSWORD: " + password);
	}

	public Map getCredentialsMap(String vcapServices) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory jsonFactory = mapper.getJsonFactory();
		JsonParser jsonParser = jsonFactory.createJsonParser(vcapServices);
		Map map = jsonParser.readValueAs(Map.class);
		List pgMap = (List) map.get("postgresql-9.0");
		Map dbMap = (Map) pgMap.get(0);
		Map credentialsMap = (Map) dbMap.get("credentials");
		return credentialsMap;
	}

}
