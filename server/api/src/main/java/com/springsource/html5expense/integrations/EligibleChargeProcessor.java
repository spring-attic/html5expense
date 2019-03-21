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
package com.springsource.html5expense.integrations;

import com.springsource.html5expense.ExpenseReportingService;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.ServiceActivator;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This is the gateway for all incoming eligible charges. There are lots of ways
 * to submit eligible charges, but we'll assume that they all converge on this
 * endpoint thanks to Spring Integration's normalization and routing prowess.
 *
 * @author Josh Long
 */
public class EligibleChargeProcessor {

    @Inject
    private ExpenseReportingService expenseReportingService;

    @ServiceActivator
    public void processNewEligibleCharge(@Header(EligibleChargeProcessorHeaders.EC_DATE) Date date,
                                         @Header(EligibleChargeProcessorHeaders.EC_MERCHANT) String merchant,
                                         @Header(EligibleChargeProcessorHeaders.EC_CATEGORY) String category,
                                         @Header(EligibleChargeProcessorHeaders.EC_AMOUNT) BigDecimal amount) throws Exception {
        this.expenseReportingService.createEligibleCharge(date, merchant, category, amount);
    }
}
