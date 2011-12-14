package com.springsource.html5expense.controllers;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.springsource.html5expense.ExpenseReportingService;

@Controller
public class ExpenseReportingViewController {

    @Inject
    private ExpenseReportingService expenseReportingService ;

    @RequestMapping(value = "/foo" ,method = RequestMethod.GET)
    public String showExpenses ( ModelMap map) throws Exception {

        return "receipts";
    }
}
