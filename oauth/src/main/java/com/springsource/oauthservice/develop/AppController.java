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
package com.springsource.oauthservice.develop;

import java.security.Principal;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/develop")
public class AppController {
	
	private final AppRepository appRepository;
	
	@Inject
	public AppController(AppRepository appRepository) {
		this.appRepository = appRepository;
	}
	
	@RequestMapping(value="/apps", method=RequestMethod.GET)
	public List<AppSummary> list(Principal user) {
		return appRepository.findAppSummaries(user.getName());
	}

	@RequestMapping(value="/apps/new", method=RequestMethod.GET)
	public AppForm newForm() {
		return appRepository.getNewAppForm();
	}
	
	@RequestMapping(value="/apps", method=RequestMethod.POST)
	public String create(Principal user, @Valid AppForm form, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "develop/apps/new";
		}
		return "redirect:/develop/apps/" + appRepository.createApp(user.getName(), form);
	}

	@RequestMapping(value="/apps/{slug}", method=RequestMethod.GET)
	public String view(@PathVariable String slug, Principal user, Model model) {
		model.addAttribute(appRepository.findAppBySlug(user.getName(), slug));
		model.addAttribute("slug", slug);
		return "develop/apps/view";
	}

	@RequestMapping(value="/apps/{slug}", method=RequestMethod.DELETE)
	public String delete(@PathVariable String slug, Principal user) {
		appRepository.deleteApp(user.getName(), slug);
		return "redirect:/develop/apps";
	}

	@RequestMapping(value="/apps/edit/{slug}", method=RequestMethod.GET)
	public String editForm(@PathVariable String slug, Principal user, Model model) {
		model.addAttribute(appRepository.getAppForm(user.getName(), slug));
		model.addAttribute("slug", slug);
		return "develop/apps/edit";
	}

	@RequestMapping(value="/apps/{slug}", method=RequestMethod.PUT)
	public String update(@PathVariable String slug, @Valid AppForm form, BindingResult bindingResult, Principal user, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("slug", slug);			
			return "develop/apps/edit";
		}
		return "redirect:/develop/apps/" + appRepository.updateApp(user.getName(), slug, form);
	}

}
