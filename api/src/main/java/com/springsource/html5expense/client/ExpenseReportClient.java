package com.springsource.html5expense.client;


import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExpenseReportClient {
    private static Log log = LogFactory.getLog(ExpenseReportClient.class);

    private String serviceUrlBase = "http://127.0.0.1:8080/reports";

    private RestTemplate restTemplate  ;

    @Inject
    public ExpenseReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long createReport(final String purpose) {
        HttpHeaders  mvp = new HttpHeaders();
        mvp.add("purpose",purpose);
        return restTemplate.postForObject(this.serviceUrlBase,  mvp, Long.class  );
    }

     public Collection<EligibleCharge> getEligibleCharges (){
         return  this.restTemplate.getForEntity( this.serviceUrlBase + "/eligible-charges", Collection.class ).getBody();
     }

    public static void main(String args[]) {
        RestTemplate restTemplate = new RestTemplate();
        ExpenseReportClient client = new ExpenseReportClient(restTemplate);

        // create a report
        Long idOfExpenseReport = client.createReport("food");
        if (log.isDebugEnabled()) {
            log.debug("Id of expense report: " + idOfExpenseReport);
        }

        // get eligible charges
        log.debug(  client.getEligibleCharges());


    }

    public Collection<Expense> createExpenses (Long reportId, Collection<EligibleCharge> eligibleCharges){

    }

    /*

    @RequestMapping(value = "/{reportId}/expenses", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody Collection<Expense> createExpenses(@PathVariable Long reportId, @RequestBody EligibleChargeList charges) {
        return service.createExpenses(reportId, charges.getChargeIds());
    }

    *//**
     * Associate an image of a receipt with an {@link Expense}
     * @param reportId the ID of the {@link com.springsource.html5expense.ExpenseReport}
     * @param expenseId the ID of the {@link Expense}
     * @param receiptBytes the image of the receipt
     * @return the URI of the image
     *//*
    @RequestMapping(value = "/{reportId}/expenses/{expenseId}/receipt", method = RequestMethod.POST, consumes = "multipart/form-data")
    public @ResponseBody String attachReceipt(@PathVariable Long reportId, @PathVariable Integer expenseId, @RequestBody byte[] receiptBytes) {
        return service.attachReceipt(reportId, expenseId, receiptBytes);
    }

    *//**
     * Finalizes and submits the {@link com.springsource.html5expense.ExpenseReport} for review
     * @param reportId the ID of the {@link com.springsource.html5expense.ExpenseReport}
     *//*
    @RequestMapping(value = "/{reportId}", method = RequestMethod.POST)
    public void submitReport(@PathVariable Long reportId) {
        service.submitReport(reportId);
    }

    *//**
     * Retrieves all of the open, or incomplete, expense reports for the user
     * @return list of {@link com.springsource.html5expense.ExpenseReport} objects
     *//*
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    List<ExpenseReport> getOpenReports() {
        return service.getOpenReports();
    }*/
}
