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
package com.springsource.html5expense;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Manages user expense reports.
 *
 * @author Keith Donald
 * @author Josh Long
 */
public interface ExpenseReportingService {

    void updateExpenseReportPurpose(Long reportId, String title);

    Collection<Expense> getExpensesForExpenseReport (  Long reportId ) ;

    /**
     * Responsible for installing new {@link EligibleCharge}s into the database
     */
    EligibleCharge createEligibleCharge(Date date, String merchant, String category, BigDecimal amt) ;

    /**
     * Creates a new expense report.
     *
     * @param purpose the purpose for this report, e.g., "Palo Alto Face to Face Meeting"
     * @return the unique ID of the expense report
     */
    Long createReport(String purpose);

    /**
     * Retrieves the charges that are eligible to be expensed.
     * The user is expected to add one or more of these charges to the report.
     *
     * @return the list of eligible charges
     */
    Collection<EligibleCharge> getEligibleCharges();

    /**
     * Adds the selected charges to the expense report.
     * Creates and returns a new expense for each charge.
     *
     * @param reportId  the expense report id
     * @param chargeIds the eligible charge ids
     * @return an expense for each charge
     */
    Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds);

    /**
     * Attach a receipt to an expense.
     *
     * @param reportId     the expense report id
     * @param receiptBytes the receipt data as a byte array
     * @param ext the extension of the uploaded media
     * @return a pointer to the receipt
     */
    String attachReceipt(Long reportId, Integer expenseId,  String ext , byte[] receiptBytes);

    /**
     * Submit the expense report for approval.
     *
     * @param reportId the id of the report to file
     */
    void submitReport(Long reportId);

    /**
     * Returns all the expense reports the user has open.
     * An open report is not under review and is not closed.
     * It can be edited by the user and {@link #submitReport(Long) submitted}.
     *
     * @return the user's open expense reports
     */
    List<ExpenseReport> getOpenReports();

    ExpenseReport getExpenseReport(Long reportId);

    Expense getExpense(Integer  expenseId);
}