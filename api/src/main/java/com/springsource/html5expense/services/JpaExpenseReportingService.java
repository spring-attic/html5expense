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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
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

    private File tmpDir = new File(SystemUtils.getUserHome(), "receipts");

    private Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void updateExpenseReportPurpose(Long reportId, String purpose) {
        ExpenseReport expenseReport = getReport(reportId);
        expenseReport.setPurpose(purpose);
        entityManager.merge(expenseReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Expense> getExpensesForExpenseReport(Long reportId) {

        Collection<Expense> expenseCollection = this.entityManager.createQuery(
                "from Expense e WHERE e.expenseReport.id  = :id", Expense.class)
                .setParameter("id", reportId)
                .getResultList();

        return expenseCollection;
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpense(Integer expenseId) {
        return entityManager.find(Expense.class, expenseId);
    }

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

    @PostConstruct
    public void begin() {
        if (!tmpDir.exists()) tmpDir.mkdirs();
    }

    @Transactional
    public String attachReceipt(Long reportId, Integer expenseId, String ext, byte[] receiptBytes) {

        String reportAndExpenseKey = "receipt-" + reportId + "-" + (expenseId) + "";
        ExpenseReport report = getReport(reportId);
        report.attachReceipt(expenseId, reportAndExpenseKey, ext);
        entityManager.merge(report);

        OutputStream out = null;
        InputStream in = null;
        try {
            File outputFile = new File(this.tmpDir, reportAndExpenseKey + "." + ext);
            in = new ByteArrayInputStream(receiptBytes);
            out = new FileOutputStream(outputFile);
            IOUtils.copy(in, out);
        } catch (Throwable th) {
            log.error(th);
        } finally {
            if (out != null) IOUtils.closeQuietly(out);
            if (in != null) IOUtils.closeQuietly(in);
        }

        return reportAndExpenseKey ;

    }

    @Transactional
    public void submitReport(Long reportId) {
        ExpenseReport entity = getReport(reportId);
        entity.markInReview();
        entityManager.merge(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public ExpenseReport getExpenseReport(Long reportId) {
        return entityManager.find(ExpenseReport.class, reportId);
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

}