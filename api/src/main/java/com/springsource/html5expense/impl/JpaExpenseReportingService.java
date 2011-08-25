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

import com.springsource.html5expense.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Josh Long
 */
@Service
public class JpaExpenseReportingService implements ExpenseReportingService {

    @PersistenceContext private EntityManager entityManager;

    @Transactional
    public Long createReport(String purpose) {
        ExpenseReportEntity report = new ExpenseReportEntity(purpose);
        entityManager.persist(report);
        return report.getId();
    }

    @Transactional(readOnly = true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return entityManager.createQuery("SELECT ec FROM EligibleCharge ec" , EligibleCharge.class).getResultList();
    }

    @Transactional
    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReportEntity report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        List<EligibleCharge> c = getEligibleCharges(chargeIds );
        for (EligibleCharge charge  : c) {
            ExpenseEntity expense = report.createExpense(charge);
            entityManager.persist(expense);
            expenses.add(expense.data());
        }
        entityManager.createQuery("DELETE FROM EligibleCharge ec WHERE ec.id IN :IDS").setParameter("IDS", chargeIds).executeUpdate();
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

    @Transactional(readOnly = true)
    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReportEntity> entities = entityManager.createQuery(
          "SELECT em FROM ExpenseReportEntity em WHERE em.state = :state", ExpenseReportEntity.class)
         .setParameter("state", State.NEW).getResultList();

        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        for (ExpenseReportEntity er : entities) {
            reports.add(er.data());
        }
        return reports;
    }

    protected ExpenseReportEntity getReport(Long reportId) {
        return entityManager.find(ExpenseReportEntity.class, reportId);
    }

    @Transactional(readOnly = true)
    protected List<EligibleCharge> getEligibleCharges(List<Long> ecIds) {
        return entityManager.createQuery("SELECT ec FROM EligibleCharge ec WHERE ec.id IN :ids", EligibleCharge.class)
                .setParameter("ids", ecIds)
                .getResultList();
    }

    //TODO !!! grab the relevant package from greenhouse
    protected String receipt(byte[] receiptBytes) {
        return "receipt for bytes";
    }
}