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
package com.springsource.html5expense.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;
import com.springsource.html5expense.State;

/**
 * @author Josh Long
 */
@Service
public class JpaExpenseReportingService implements ExpenseReportingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long createReport(String purpose) {
        ExpenseReportEntity report = new ExpenseReportEntity(purpose);
        entityManager.persist(report);
        return report.getId();
    }

    @Transactional(readOnly=true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return entityManager.createQuery("from EligibleCharge", EligibleCharge.class).getResultList();
    }

    @Transactional
    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReportEntity report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        List<EligibleCharge> charges = getEligibleCharges(chargeIds);
        for (EligibleCharge charge : charges) {
            ExpenseEntity expense = report.createExpense(charge);
            entityManager.persist(expense);
            expenses.add(expense.data());
        }
        // todo restore removeAddedCharges(chargeIds);
        return expenses;
    }

    @Transactional
    public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
        ExpenseReportEntity report = getReport(reportId);
        String receipt = receipt(receiptBytes);
        report.attachReceipt(expenseId, receipt);
        entityManager.merge(report);
        return receipt;
    }

    @Transactional
    public void submitReport(Long reportId) {
        ExpenseReportEntity entity = getReport(reportId);
        entity.markInReview();
        entityManager.merge(entity);
    }

    @Transactional(readOnly=true)
    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        List<ExpenseReportEntity> entities = entityManager.createQuery("from ExpenseReportEntity er where er.state = :new or er.state = :rejected", ExpenseReportEntity.class)
                 .setParameter("new", State.NEW)
                 .setParameter("rejected", State.REJECTED)
                 .getResultList();

        for (ExpenseReportEntity report : entities) {
            reports.add(report.data());
        }
        return reports;
    }

    // internal helpers
    
    private ExpenseReportEntity getReport(Long reportId) {
        return entityManager.find(ExpenseReportEntity.class, reportId);
    }

    private List<EligibleCharge> getEligibleCharges(List<Long> chargeIds) {
        return entityManager.createQuery("from EligibleCharge where id in :ids", EligibleCharge.class).setParameter("ids", chargeIds).getResultList();
    }

    private void removeAddedCharges(List<Long> chargeIds) {
        entityManager.createQuery("delete from EligibleCharge where id in :ids").setParameter("ids", chargeIds).executeUpdate();
    }
    
    private String receipt(byte[] receiptBytes) {
        // TODO
        return "receipt for bytes";
    }
}