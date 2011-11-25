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
import org.apache.commons.collections.CollectionUtils;
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
import java.util.*;

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

    @Override
    public File retreiveReceipt(Integer expenseId) {
        Expense e = getExpense(expenseId);
        return fileForExpense(e);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void updateExpenseReportPurpose(Long reportId, String purpose) {
        ExpenseReport expenseReport = getReport(reportId);
        expenseReport.setPurpose(purpose);
        entityManager.merge(expenseReport);
    }

    static private class ExpenseComparator implements Comparator<Expense> {
        @Override
        public int compare(Expense expense, Expense expense1) {
            return expense.getId().compareTo(expense1.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Expense> getExpensesForExpenseReport(Long reportId) {

        List<Expense> expenseCollection = this.entityManager.createQuery(
                "from Expense e WHERE e.expenseReport.id  = :id", Expense.class)
                .setParameter("id", reportId)
                .getResultList();

        // consistant sorting
        Collections.sort(expenseCollection, new ExpenseComparator());

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


    @Transactional
    public void deleteExpenseReport(Long expenseReportId) {
        Collection<Expense> expenses;
        expenses = getExpensesForExpenseReport(expenseReportId);
        if (expenses.size() > 0) {
            List<Integer> ids = new ArrayList<Integer>();
            for (Expense e : expenses)
                ids.add(e.getId());
            restoreEligibleCharges(ids);
        }
        expenses = getExpensesForExpenseReport(expenseReportId);
        log.debug("there are " + expenses.size() + " expenses  in the report #" + expenseReportId);
        ExpenseReport expenseReport = getExpenseReport( expenseReportId);
        entityManager.remove(expenseReport);
        entityManager.flush();

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
    @Override
    public void restoreEligibleCharges(List<Integer> expenseIds) {
        for (Integer l : expenseIds) {
            Expense e = getExpense(l);
            ExpenseReport expenseReport = e.getExpenseReport();
            EligibleCharge eligibleCharge = createEligibleCharge(e.getDate(), e.getMerchant(), e.getCategory(), e.getAmount());
            if (eligibleCharge != null) {
                entityManager.remove(e);
            }
        }
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


    private String keyForExpenseReceipt(Long reportId, Integer expenseId) {

        String reportAndExpenseKey = "receipt-" + reportId + "-" + (expenseId) + "";
        return reportAndExpenseKey;
    }

    private String fileNameForReceipt(String key, String ext) {
        return key + "." + ext;
    }

    private File fileForExpense(Expense ex) {
        return new File(this.tmpDir, fileNameForReceipt(ex.getReceipt(), ex.getReceiptExtension()));
    }

    @Transactional
    public String attachReceipt(Long reportId, Integer expenseId, String ext, byte[] receiptBytes) {

        String reportAndExpenseKey = keyForExpenseReceipt(reportId, expenseId);
        Expense expense = getExpense(expenseId);
        ExpenseReport report = getReport(reportId);
        report.attachReceipt(expenseId, reportAndExpenseKey, ext);
        entityManager.merge(report);

        OutputStream out = null;
        InputStream in = null;
        try {
            File outputFile = fileForExpense(expense);
            in = new ByteArrayInputStream(receiptBytes);
            out = new FileOutputStream(outputFile);
            IOUtils.copy(in, out);
        } catch (Throwable th) {
            log.error(th);
        } finally {
            if (out != null) IOUtils.closeQuietly(out);
            if (in != null) IOUtils.closeQuietly(in);
        }

        return reportAndExpenseKey;

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