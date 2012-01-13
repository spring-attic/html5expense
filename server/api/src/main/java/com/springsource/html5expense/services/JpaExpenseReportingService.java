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
import com.springsource.html5expense.services.utilities.MongoDbGridFsUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Implementation of the business logic for the expense reports and expense report charges.
 *
 * @author Josh Long
 * @author Roy Clarkson
 * @see ExpenseReportingService
 */
@Service
@Transactional
public class JpaExpenseReportingService implements ExpenseReportingService {

    private String mongoDbGridFsFileBucket = "expenseReports" ;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject private MongoTemplate mongoTemplate ;

    private File tmpDir = new File(SystemUtils.getUserHome(), "receipts");

    private Log log = LogFactory.getLog(getClass());
    
    private static class ExpenseComparator implements Comparator<Expense> {
        @Override
        public int compare(Expense expense, Expense expense1) {
            return expense.getId().compareTo(expense1.getId());
        }
    }

    public InputStream retrieveReceipt(Integer expenseId) {
        Expense e = getExpense(expenseId);
        String fn = fileNameForReceipt(e ) ;
        return MongoDbGridFsUtils.read( this.mongoTemplate, this.mongoDbGridFsFileBucket, fn) ;
    }

    public void updateExpenseReportPurpose(Long reportId, String purpose) {
        ExpenseReport expenseReport = getReport(reportId);
        expenseReport.setPurpose(purpose);
        entityManager.merge(expenseReport);
    }

    @Transactional(readOnly = true)
    public Collection<Expense> getExpensesForExpenseReport(Long reportId) {
        List<Expense> expenseCollection = this.entityManager.createQuery(
                "SELECT e FROM " + Expense.class.getName() + " e WHERE e.expenseReport.id  = :id", Expense.class)
                .setParameter("id", reportId)
                .getResultList();

        // consistent sorting
        Collections.sort(expenseCollection, new ExpenseComparator());

        return expenseCollection;
    }

    @Transactional(readOnly = true)
    public Expense getExpense(Integer expenseId) {
        return entityManager.find(Expense.class, expenseId);
    }

    @Transactional(readOnly = true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return entityManager.createQuery("SELECT er from " + EligibleCharge.class.getName() + " er ", EligibleCharge.class).getResultList();
    }

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
        ExpenseReport expenseReport = getExpenseReport(expenseReportId);
        entityManager.remove(expenseReport);
        entityManager.flush();
    }

    public EligibleCharge createEligibleCharge(Date date, String merchant, String category, BigDecimal amt) {
        EligibleCharge charge = new EligibleCharge(date, merchant, category, amt);
        entityManager.persist(charge);
        return charge;
    }

    public Long createReport(String purpose) {
        ExpenseReport report = new ExpenseReport(purpose);
        entityManager.persist(report);
        return report.getId();
    }

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

    private String keyForExpenseReceipt(Long reportId, Integer expenseId) {
        return "receipt-" + reportId + "-" + (expenseId) + "";
    }

    private String fileNameForReceipt(String key, String ext) {
        return key + "." + ext;
    }

    public String attachReceipt(Long reportId, Integer expenseId, String ext, byte[] receiptBytes) {
        String reportAndExpenseKey = keyForExpenseReceipt(reportId, expenseId);
        Expense expense = getExpense(expenseId);
        ExpenseReport report = getReport(reportId);
        report.attachReceipt(expenseId, reportAndExpenseKey, ext);
        entityManager.merge(report);
        writeExpense( expense, receiptBytes);
        return reportAndExpenseKey;
    }

    private void writeExpense(Expense expense, byte[] receiptBytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( receiptBytes) ;     
        String fn = fileNameForReceipt( expense ); 
        MongoDbGridFsUtils.write( this.mongoTemplate,  this.mongoDbGridFsFileBucket, byteArrayInputStream, fn , null );
    }       
    
    private String fileNameForReceipt( Expense e ) {
        return fileNameForReceipt( e.getReceipt(), e.getReceiptExtension()) ;
    }

    public void submitReport(Long reportId) {
        ExpenseReport entity = getReport(reportId);
        entity.markInReview();
        entityManager.merge(entity);
    }

    @Transactional(readOnly = true)
    public ExpenseReport getExpenseReport(Long reportId) {
        return entityManager.find(ExpenseReport.class, reportId);
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        List<ExpenseReport> entities = entityManager.createQuery(
                "SELECT e from " + ExpenseReport.class.getName() + " e where e.state = :new or e.state = :rejected", ExpenseReport.class)
                .setParameter("new", State.NEW)
                .setParameter("rejected", State.REJECTED)
                .getResultList();

        for (ExpenseReport report : entities) {
            reports.add(report);
        }

        return reports;
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getSubmittedReports() {
        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        List<ExpenseReport> entities = entityManager.createQuery(
                "select er from " + ExpenseReport.class.getName() + " er where er.state = :in_review or er.state = :approved", ExpenseReport.class)
                .setParameter("in_review", State.IN_REVIEW)
                .setParameter("approved", State.APPROVED)
                .getResultList();

        for (ExpenseReport report : entities) {
            reports.add(report);
        }
        return reports;
    }

    @Transactional(readOnly = true)
    private ExpenseReport getReport(Long reportId) {
        return entityManager.find(ExpenseReport.class, reportId);
    }

    private List<EligibleCharge> getEligibleCharges(List<Long> chargeIds) {
        return entityManager.createQuery("select e from " + EligibleCharge.class.getName() + " e where e.id in :ids", EligibleCharge.class)
                .setParameter("ids", chargeIds)
                .getResultList();
    }

    private void removeAddedCharges(List<Long> chargeIds) {
        entityManager.createQuery("delete from " + EligibleCharge.class.getName() + " e where e.id in :ids")
                .setParameter("ids", chargeIds)
                .executeUpdate();
    }

}