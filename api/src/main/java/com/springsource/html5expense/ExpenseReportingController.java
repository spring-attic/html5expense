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
package com.springsource.html5expense;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Roy Clarkson
 */
@Controller
public class ExpenseReportingController {
	
	private final ExpenseReportingService service;
	
	@Inject
	public ExpenseReportingController (ExpenseReportingService expenseReportingService) {
		this.service = expenseReportingService;
	}

	@RequestMapping(value="report/create", method=RequestMethod.POST)
	public @ResponseBody Long createReport(@RequestParam String purpose) {
		// TODO: should this throw an exception if the purpose is empty?
		return service.createReport(purpose);
	}
	
	@RequestMapping(value="charges", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Collection<EligibleCharge> getEligibleCharges() {
		return service.getEligibleCharges();
	}
	
	@RequestMapping(value="report/{reportId}/expenses/create", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody Collection<Expense> createExpenses(@PathVariable Long reportId, @RequestParam List<Long> chargeIds) {
		// TODO: should this throw an exception if the reportId is not valid?
		return service.createExpenses(reportId, chargeIds);
	}
	
	@RequestMapping(value="report/{reportId}/expense/{expenseId}/receipt/attach", method=RequestMethod.POST, consumes="multipart/form-data")
	public @ResponseBody String attachReceipt(@PathVariable Long reportId, @PathVariable Integer expenseId, @RequestParam byte[] receiptBytes) {
		// TODO: should this throw an exception if the reportId is not valid?
		// TODO: should this throw an exception if the expenseId is not valid?
		return service.attachReceipt(reportId, expenseId, receiptBytes);
	}
	
	@RequestMapping(value="report/{reportId}/submit")
	public void submitReport(@PathVariable Long reportId) {
		// TODO: what response to return?
		// TODO: should this throw an exception if the reportId is not valid?
		service.submitReport(reportId);
	}

	@RequestMapping(value="reports/open", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody List<ExpenseReport> getOpenReports() {
		return service.getOpenReports();
	}
	
}
