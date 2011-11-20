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
package com.springsource.html5expense.controllers;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * @author Roy Clarkson
 * @author Josh Long
 */
@Controller
@RequestMapping("/reports")
public class ExpenseReportingController {

    @Inject
    private ExpenseReportingService service;

    /**
     * Create a new {@link com.springsource.html5expense.ExpenseReport} with an associated description for the purpose
     *
     * @param purpose the reason for the expense report. i.e. conference, business meal, etc.
     * @return the ID of the new expense report
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Long createReport(@RequestParam(required = true) String purpose) {
        return service.createReport(purpose);
    }

    /**
     * Retrieve a list of charges that can be associated with an {@link com.springsource.html5expense.ExpenseReport}.
     * These charges are not currently associated with any other expense report.
     *
     * @return collection of {@link com.springsource.html5expense.EligibleCharge} objects
     */
    @RequestMapping(value = "/eligible-charges", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<EligibleCharge> getEligibleCharges() {
        return service.getEligibleCharges();
    }

    /**
     * Associate expenses with an {@link com.springsource.html5expense.ExpenseReport}
     *
     * @param reportId  the ID of the {@link com.springsource.html5expense.ExpenseReport}
     * @param chargeIds the IDs of the {@link EligibleCharge} objects to associate with the expense report
     * @return
     */
    @RequestMapping(value = "/{reportId}/expenses", method = RequestMethod.POST, produces = "application/json")

    @ResponseBody
    public Collection<Expense> createExpenses(
            @PathVariable Long reportId,
            @RequestParam(required = true) List<Long> chargeIds) {
        return service.createExpenses(reportId, chargeIds);
    }

    /**
     * Associate an image of a receipt with an {@link Expense}
     *
     * @param reportId     the ID of the {@link com.springsource.html5expense.ExpenseReport}
     * @param expenseId    the ID of the {@link Expense}
     * @param receiptBytes the image of the receipt
     * @return the URI of the image
     */
    @RequestMapping(value = "/{reportId}/expenses/{expenseId}/receipt",
            method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseBody
    public String attachReceipt(@PathVariable Long reportId, @PathVariable Integer expenseId, @RequestParam(required = true) byte[] receiptBytes) {
        return service.attachReceipt(reportId, expenseId, receiptBytes);
    }

    /**
     * Finalizes and submits the {@link com.springsource.html5expense.ExpenseReport} for review
     *
     * @param reportId the ID of the {@link com.springsource.html5expense.ExpenseReport}
     */
    @RequestMapping(value = "/{reportId}", method = RequestMethod.POST)
    public void submitReport(@PathVariable Long reportId) {
        service.submitReport(reportId);
    }

    /**
     * Retrieves all of the open, or incomplete, expense reports for the user
     *
     * @return list of {@link com.springsource.html5expense.ExpenseReport} objects
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ExpenseReport> getOpenReports() {
        return service.getOpenReports();
    }

}
