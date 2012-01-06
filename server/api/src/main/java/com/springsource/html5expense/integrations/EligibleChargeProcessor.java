package com.springsource.html5expense.integrations;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.ServiceActivator;

import com.springsource.html5expense.ExpenseReportingService;

/**
 * This is the gateway for all incoming eligible charges.
 *
 * There are lots of ways to submit eligible charges, but we'll assume that they all converge on this
 * endpoint thanks to Spring Integration's normalization and routing prowess.
 *
 * @author Josh Long
 */

public class EligibleChargeProcessor {

    @Inject
    private ExpenseReportingService expenseReportingService ;

    @ServiceActivator
    public void processNewEligibleCharge( @Header(EligibleChargeProcessorHeaders.EC_DATE) Date date,
                                          @Header(EligibleChargeProcessorHeaders.EC_MERCHANT) String merchant,
                                          @Header(EligibleChargeProcessorHeaders.EC_CATEGORY) String category,
                                          @Header(EligibleChargeProcessorHeaders.EC_AMOUNT) BigDecimal amount ) throws Exception {
        this.expenseReportingService.createEligibleCharge( date,  merchant, category, amount) ;
    }
}
