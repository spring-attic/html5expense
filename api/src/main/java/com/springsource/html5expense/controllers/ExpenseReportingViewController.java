package com.springsource.html5expense.controllers;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.ExpenseReportingService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

@Controller
public class ExpenseReportingViewController {

    @Inject
    private ExpenseReportingService expenseReportingService ;

    @RequestMapping(value = "/" ,method = RequestMethod.GET)
    public String showExpenses ( ModelMap map) throws Exception {
     //   Collection<EligibleCharge> eligibleCharges = this.expenseReportingService.getEligibleCharges() ;
        return "receipts";
    }
}
