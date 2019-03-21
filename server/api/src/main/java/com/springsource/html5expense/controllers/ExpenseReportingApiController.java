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
package com.springsource.html5expense.controllers;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private Log log = LogFactory.getLog(getClass());

    @Inject
    private ExpenseReportingService service;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.DELETE, value = "/expenses/{expenseId}")
    public void restoreExpenseToEligibleCharge(@PathVariable("expenseId") Integer expenseId) {
        Expense ex = service.getExpense(expenseId);
        service.restoreEligibleCharges(Arrays.asList(ex.getId()));
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{reportId}/expenses", produces = "application/json")
    @ResponseBody
    public Collection<Expense> expenseForExpenseReport(HttpServletRequest request, @PathVariable("reportId") Long reportId) {
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

    private String buildMimeTypeForExpense(Expense e) {
        String ext = e.getReceiptExtension();
        String mime = null;
        if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
            mime = "image/jpeg";
        } else if (ext.equalsIgnoreCase("gif"))
            mime = "image/gif";
        else
            mime = "application/binary";
        return mime;
    }


    @RequestMapping(value = "/receipts/{expenseId}")
    public void renderMedia(HttpServletResponse httpServletResponse, OutputStream os, @PathVariable("expenseId") Integer expenseId) {

        Expense expense = service.getExpense(expenseId);
        httpServletResponse.setContentType(buildMimeTypeForExpense(expense));
        InputStream is = service.retrieveReceipt(expenseId);
        try {
            IOUtils.copyLarge(is, os);
        } catch (Exception e1) {
            log.error(e1);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        /* 
        try {
            is = new FileInputStream(f);
            httpServletResponse.setContentLength((int) f.length());
            IOUtils.copyLarge(is, os);
        } catch (Exception e1) {
            log.error(e1);
        } finally {
            if (is != null)
                IOUtils.closeQuietly(is);
            if (os != null)
                IOUtils.closeQuietly(os);
        }*/
    }


    @RequestMapping(value = "/{reportId}/expenses", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Collection<Expense> createExpenses(@PathVariable Long reportId, @RequestParam(required = true, value = "chargeId") Long chargeId) {
        return service.createExpenses(reportId, Arrays.asList(chargeId));
    }

    private String findExtensionFromFileName(String fn) {
        int lPosOfPeriod = fn.lastIndexOf(".");
        if (lPosOfPeriod != -1 && !fn.endsWith(".")) {
            return fn.substring(lPosOfPeriod + 1);
        }
        return null;
    }

    @RequestMapping(value = "/receipts", method = RequestMethod.POST)
    @ResponseBody
    public String attachReceipt(@RequestParam("reportId") Long reportId, @RequestParam("expenseId") Integer expenseId, @RequestParam("file") MultipartFile file) {
        try {
            byte[] bytesForImage = file.getBytes();
            String ext = findExtensionFromFileName(file.getOriginalFilename());
            if (ext != null) {
                ext = ext.trim().toLowerCase();
            }
            return service.attachReceipt(reportId, expenseId, ext, bytesForImage);
        } catch (Throwable th) {
            if (log.isErrorEnabled()) {
                log.error("Something went wrong trying to write the file out.", th);
            }
        }
        return null;
    }


    /**
     * Finalizes and submits the {@link com.springsource.html5expense.ExpenseReport} for review
     *
     * @param reportId the ID of the {@link com.springsource.html5expense.ExpenseReport}
     */
    @RequestMapping(value = "/{reportId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void submitReport(@PathVariable("reportId") Long reportId) {
        service.submitReport(reportId);
    }

    @RequestMapping(value = "/{reportId}", method = RequestMethod.GET)
    @ResponseBody
    public ExpenseReport getReport(@PathVariable("reportId") Long reportId) {
        return service.getExpenseReport(reportId);
    }

    @RequestMapping(value = "/{reportId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteReport(@PathVariable("reportId") Long reportId) {
        this.service.deleteExpenseReport(reportId);
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

    /**
     * Retrieves all of the submitted expense reports for the user
     *
     * @return list of {@link ExpenseReport} objects
     */
    @RequestMapping(value = "/submitted", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<ExpenseReport> getSubmittedReports() {
        return service.getSubmittedReports();
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
