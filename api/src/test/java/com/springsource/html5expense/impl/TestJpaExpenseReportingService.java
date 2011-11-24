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
import com.springsource.html5expense.config.ComponentConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = ComponentConfig.class)
public class TestJpaExpenseReportingService {
    private String itsMission = "To boldly go where no application's gone before!";

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ExpenseReportingService expenseReportingService;

    @Inject
    private PlatformTransactionManager transactionManager;

    private EligibleCharge expensiveCharge;
    private List<EligibleCharge> charges;


    @Before
    public void installSomeCharges() throws Throwable {
        final List<EligibleCharge> chargesToAdd = Arrays.asList(
                new EligibleCharge(new Date(), "Starbucks", "food", new BigDecimal(4)),
                new EligibleCharge(new Date(), "dinner at Morton's Steak House", "food", new BigDecimal(59.99)));

        // clean out the data
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        transactionTemplate.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {

                entityManager.createQuery("DELETE FROM " + Expense.class.getName()).executeUpdate();
                entityManager.createQuery("DELETE FROM " + ExpenseReport.class.getName()).executeUpdate();
                entityManager.createQuery("DELETE FROM " + EligibleCharge.class.getName()).executeUpdate();
                return null;
            }
        });

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (EligibleCharge ec : chargesToAdd) {
                    entityManager.persist(ec);
                }
            }
        });

        charges = new ArrayList<EligibleCharge>(expenseReportingService.getEligibleCharges());
        expensiveCharge = charges.get(1);
    }


    @Test
    public void testCreateReport() throws Throwable {
        Long expenseReportId = expenseReportingService.createReport(itsMission);
        Assert.assertTrue("the ID must be greater than 0", expenseReportId > 0);
        Assert.assertEquals(1, expenseReportingService.getOpenReports().size());
        Assert.assertEquals(entityManager.find(ExpenseReport.class, expenseReportId).getPurpose(), itsMission);
    }

    @Test
    public void testIdentifyingEligibleCharges() throws Throwable {
        Collection<EligibleCharge> eligibleCharges = expenseReportingService.getEligibleCharges();
        Assert.assertTrue(eligibleCharges.size() == this.charges.size());
    }

    @Test
    public void testCreateExpenses() throws Throwable {
        Long expenseReportId = expenseReportingService.createReport(itsMission);
        List<Long> chargeIds = new ArrayList<Long>();
        for (EligibleCharge ec : charges) {
            chargeIds.add(ec.getId());
        }
        Collection<Expense> expenseCollection = expenseReportingService.createExpenses(expenseReportId, chargeIds);
        Assert.assertNotNull(expenseCollection);
        Assert.assertTrue(expenseCollection.size() == 2);
    }
/*

    @Test public void testAttachingReceipts() throws Throwable {
    Long expenseReportId = expenseReportingService.createReport(itsMission);
    Collection<Expense> expenses = expenseReportingService.createExpenses(expenseReportId, Arrays.asList(expensiveCharge.getId()));
    Integer expenseId = expenses.iterator().next().getId();
    String receiptClaim = expenseReportingService.attachReceipt(expenseReportId, expenseId, new byte[0]);
    Assert.assertNotNull(receiptClaim);
    Expense entity = entityManager.find(Expense.class, expenseId);
    Assert.assertFalse(entity.isFlagged());
    Assert.assertEquals(receiptClaim, entity.getReceipt());
    }
*/

    @Test
    @Transactional
    public void testSubmittingReports() throws Throwable {
        Long expenseReportId = expenseReportingService.createReport(itsMission);
        expenseReportingService.submitReport(expenseReportId);
        ExpenseReport er = entityManager.find(ExpenseReport.class, expenseReportId);
        Assert.assertEquals(er.getState(), State.IN_REVIEW);
    }

}
/**

 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = ComponentConfig.class)
 public class TestJpaExpenseReportingService {

 private EligibleCharge expensiveCharge;

 @Inject private ExpenseReportingService expenseReportingService;

 @Inject private PlatformTransactionManager transactionManager;

 @PersistenceContext private EntityManager entityManager;

 @Inject private DataSource dataSource;

 private String itsMission = "\"to go... where no man... has gone before!\"";

 private List<EligibleCharge> charges;

 }
 */