package com.springsource.html5expense.client;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * Client of our REST endpoint
 *
 * @author Josh Long
 */
public class Main {

    private static Log log = LogFactory.getLog(ExpenseReportClient.class);

    @Configuration
    public static class ExpenseReportClientConfiguration {
        @Bean
        public ExpenseReportClient client() {
            return new ExpenseReportClient("http://127.0.0.1:8080");
        }
    }

    public static void main(String args[]) {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ExpenseReportClientConfiguration.class);

        ExpenseReportClient client = annotationConfigApplicationContext.getBean(ExpenseReportClient.class);

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
        for (ExpenseReport er : reports) {
            log.debug("expense report: " + er.toString());
        }

        client.submitReport(idOfExpenseReport);
    }

}
