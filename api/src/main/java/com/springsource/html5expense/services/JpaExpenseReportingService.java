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
package com.springsource.html5expense.services;

import com.springsource.html5expense.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Implementation of the business logic for the expense reports and expense report charges.
 *
 * @author Josh Long
 * @see ExpenseReportingService
 */
@Service
public class JpaExpenseReportingService implements ExpenseReportingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return entityManager.createQuery("from EligibleCharge", EligibleCharge.class).getResultList();
    }

    @Override
    @Transactional
    public EligibleCharge createEligibleCharge(Date date, String merchant, String category, BigDecimal amt) {
        EligibleCharge charge = new EligibleCharge(date, merchant, category, amt);
        entityManager.persist(charge);
        return charge;
    }

    @Transactional
    public Long createReport(String purpose) {
        ExpenseReport report = new ExpenseReport(purpose);
        entityManager.persist(report);
        return report.getId();
    }

    @Transactional
    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReport report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        List<EligibleCharge> charges = getEligibleCharges(chargeIds);
        for (EligibleCharge charge : charges) {
            Expense expense = report.createExpense(charge);
            entityManager.persist(expense);
            expenses.add(expense);
        }
        removeAddedCharges(chargeIds);
        return expenses;
    }

    @Transactional
    public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
        ExpenseReport report = getReport(reportId);
        String receipt = receipt(receiptBytes);
        report.attachReceipt(expenseId, receipt);
        entityManager.merge(report);
        return receipt;
    }

    @Transactional
    public void submitReport(Long reportId) {
        ExpenseReport entity = getReport(reportId);
        entity.markInReview();
        entityManager.merge(entity);
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        List<ExpenseReport> entities = entityManager.createQuery(
                "from ExpenseReport where state = :new or state = :rejected", ExpenseReport.class)
                .setParameter("new", State.NEW)
                .setParameter("rejected", State.REJECTED)
                .getResultList();

        for (ExpenseReport report : entities) {
            reports.add(report);
        }

        return reports;
    }

    private ExpenseReport getReport(Long reportId) {
        return entityManager.find(ExpenseReport.class, reportId);
    }

    private List<EligibleCharge> getEligibleCharges(List<Long> chargeIds) {
        return entityManager.createQuery("from EligibleCharge where id in :ids", EligibleCharge.class)
                .setParameter("ids", chargeIds)
                .getResultList();
    }

    private void removeAddedCharges(List<Long> chargeIds) {
        entityManager.createQuery("delete from EligibleCharge where id in :ids")
                .setParameter("ids", chargeIds)
                .executeUpdate();
    }

    private String receipt(byte[] receiptBytes) {
        return "receipt for bytes";
    }
}