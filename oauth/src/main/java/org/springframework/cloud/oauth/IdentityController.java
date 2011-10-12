package org.springframework.cloud.oauth;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentityController {

	@RequestMapping(value="/me", method=RequestMethod.GET)
	public @ResponseBody String getIdentity(Principal principal) {
		return principal.getName();
	}
	
}
