package com.springsource.html5expense.client;


import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Seed the data; insert into eligiblecharge ( amount ,category ,date ,merchant ) values( 23.4,'food',  now(), 'Starbucks')
 *
 * @author Josh Long
 */

public class ExpenseReportClient {

    private String serviceUrlBase;

    private RestTemplate restTemplate;

    public ExpenseReportClient(String host) {
        this(new RestTemplate(), host);
    }

    public ExpenseReportClient(RestTemplate restTemplate, String host) {
        this.restTemplate = restTemplate;
        this.serviceUrlBase = host + "/reports";
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

    public void submitReport(Long reportId) {
        restTemplate.postForEntity(this.serviceUrlBase + "/{reportId}", null, null, reportId);
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



