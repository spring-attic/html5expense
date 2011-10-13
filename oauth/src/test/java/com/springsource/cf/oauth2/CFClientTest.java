package com.springsource.cf.oauth2;

import java.util.List;

import org.cloudfoundry.client.lib.CloudApplication;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Test;

public class CFClientTest {
	
	@Test
	public void tryClient() throws Exception {
		
		CloudFoundryClient client = new CloudFoundryClient("cwalls@vmware.com", "Habuma#1", "https://api.cloudfoundry.com");
		client.login();
		List<CloudApplication> applications = client.getApplications();
		for (CloudApplication cloudApplication : applications) {
			System.out.println(cloudApplication.getName() + " :: " + cloudApplication.getState());
			List<String> uris = cloudApplication.getUris();
			for (String uri : uris) {
				System.out.println("   --  " + uri);
			}
		}

	}
}
