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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Roy Clarkson
 * @author Josh Long
 */
@Controller
@RequestMapping("/reports")
public class ExpenseReportingApiController {

    //
//    @RequestMapping (value= "/ids", method = RequestMethod.GET, produces = "application/json")
//    @ResponseBody public List <Long> ids(){
//    return   Arrays. <Long>asList(2L, 4L, 45L, 53432L);
    private File tmpDir = new File(SystemUtils.getUserHome(), "receipts");

    @Inject
    private ExpenseReportingService service;

    @RequestMapping(method = RequestMethod.GET, value = "/{reportId}/expenses", produces = "application/json")
    @ResponseBody
    public Collection<Expense> expenseForExpenseReport(@PathVariable(value = "reportId") Long reportId) {
        return this.service.getExpensesForExpenseReport(reportId);
    }

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


//    }

    @RequestMapping(value = "/{reportId}/expenses", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Collection<Expense> createExpenses(
            @PathVariable Long reportId, @RequestParam(required = true, value = "chargeId") Long chargeId) {

        Collection<Expense> expenseCollection = service.createExpenses(reportId, Arrays.asList(chargeId));

        return expenseCollection;
    }


    @RequestMapping(value = "/receipts", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void attachReceipt(@RequestParam("file") MultipartFile file) {
        System.out.println("Received an upload with file " + file.getName());
        try {
            File outputFile = new File(this.tmpDir, System.currentTimeMillis() + ".jpg");
            InputStream in = file.getInputStream();
            OutputStream out = new FileOutputStream(outputFile);
            IOUtils.copy(in, out);
            System.out.println("wrote " + file.getName() + " to " + outputFile.getAbsolutePath());
            //return "receipts";
        } catch (Throwable th) {
            System.out.println(ExceptionUtils.getFullStackTrace(th));
        }
    }

    @RequestMapping(value = "/{reportId}/expenses/{expenseId}/receipt",
            method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseBody
    public String attachReceipt(@PathVariable Long reportId,
                                @PathVariable Integer expenseId,
                                @RequestParam(required = true) byte[] receiptBytes) {
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

    @RequestMapping(value = "/{reportId}", method = RequestMethod.GET)
    @ResponseBody
    public ExpenseReport getReport(@PathVariable Long reportId) {
        return service.getExpenseReport(reportId);
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

    @RequestMapping(method = RequestMethod.POST, value = "/{reportId}/purpose")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateReportPurpose(@PathVariable("reportId") Long reportId, String title) {
        service.updateExpenseReportPurpose(reportId, title);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/open-reports")
    @ResponseBody
    public Collection<ExpenseReport> openReports() {
        return service.getOpenReports();
    }

}
