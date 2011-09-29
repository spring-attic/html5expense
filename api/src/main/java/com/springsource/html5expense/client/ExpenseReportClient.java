package com.springsource.html5expense.client;


import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExpenseReportClient {

    private static Log log = LogFactory.getLog(ExpenseReportClient.class);

    private String serviceUrlBase = "http://127.0.0.1:8080/reports";

    private RestTemplate restTemplate;

    @Inject
    public ExpenseReportClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long createReport(final String purpose) {
        HttpHeaders mvp = new HttpHeaders();
        mvp.add("purpose", purpose);
        return restTemplate.postForObject(this.serviceUrlBase, mvp, Long.class);
    }

    public Collection<EligibleCharge> getEligibleCharges() {
        String ecUrl = this.serviceUrlBase + "/eligible-charges";
        return this.restTemplate.getForObject(ecUrl, EligibleChargeList.class);
    }

    public Collection<Expense> createExpenses(Long reportId, Collection<EligibleCharge> eligibleCharges) {
        String expensesUrl = this.serviceUrlBase + "/{reportId}/expenses";
        List<Long> ids = new ArrayList<Long>();
        for (EligibleCharge ec : eligibleCharges) {
            ids.add(ec.getId());
        }
        return restTemplate.postForObject(expensesUrl, new com.springsource.html5expense.EligibleChargeList(ids), ExpenseList.class, reportId);
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
        Collection<EligibleCharge> eligibleCharges = client.getEligibleCharges();
        log.debug(eligibleCharges);

        for (EligibleCharge charge : eligibleCharges) {
            log.debug("charge:" + charge);
        }

        Collection<Expense> expenses = client.createExpenses(idOfExpenseReport, eligibleCharges);
        log.debug("the expenses are " + expenses);
        for (Expense ex : expenses) {
            log.debug("expense=" + ex);
        }

        List<ExpenseReport> reports = client.getOpenReports();
        log.debug("open reports:" + reports);

        for (ExpenseReport er : reports) {
            log.debug(er.toString());
        }


    }

    public List<ExpenseReport> getOpenReports() {
        return restTemplate.getForObject(this.serviceUrlBase, ExpenseReportList.class);
    }

    static private class EligibleChargeList extends ArrayList<EligibleCharge> {
    }

    static private class ExpenseList extends ArrayList<Expense> {
    }

    static private class ExpenseReportList extends ArrayList<ExpenseReport> {
    }

}

/*


*//**

 ///
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

