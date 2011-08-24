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


import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReportingService;
import com.springsource.html5expense.config.ComponentConfig;
import junit.framework.Assert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = ComponentConfig.class)
public class TestJpaExpenseReportingService {
	@Inject
	private ExpenseReportingService expenseReportingService;

	@Inject private PlatformTransactionManager transactionManager;

	@PersistenceContext private EntityManager entityManager;

	private TransactionTemplate transactionTemplate;

	private String itsMission = "\"to go... where no man... has gone before!\"";
	private EligibleChargeEntity inExpensiveExpense, expensiveExpense;
	private List<Long> charges = new ArrayList<Long>();

	@Before
	public void installSomeCharges() throws Throwable {

		// kind of cheating since i dont want to expose a separate Charge service, so well force-feed the database some sample data
		transactionTemplate = new TransactionTemplate(this.transactionManager);
		this.charges = transactionTemplate.execute(new TransactionCallback<List<Long>>() {
			public List<Long> doInTransaction(TransactionStatus status) {
				inExpensiveExpense = new EligibleChargeEntity(new LocalDate(), "Starbucks", "food", new BigDecimal(4));
				entityManager.persist(inExpensiveExpense);

				expensiveExpense = new EligibleChargeEntity(new LocalDate(), "dinner at Morton's Steak House", "food", new BigDecimal(59.99));
				entityManager.persist(expensiveExpense);
				return Arrays.asList(inExpensiveExpense.getId(), expensiveExpense.getId());
			}
		});
	}

	@Test
	public void testIdentifyingEligibleCharges() throws Throwable {
		Collection<EligibleCharge> eligibleCharges = expenseReportingService.getEligibleCharges();
		Assert.assertTrue(eligibleCharges.size() == this.charges.size());
	}

	@Test
	public void testCreateReport() throws Throwable {
		Long expenseReportId = expenseReportingService.createReport(itsMission);
		Assert.assertTrue("the ID must be greater than 0", expenseReportId > 0);
	}

	@Test
	public void testCreateExpenses() throws Throwable {
		Long expenseReportId = expenseReportingService.createReport(itsMission);
		Collection<Expense> expenseCollection = expenseReportingService.createExpenses(expenseReportId, this.charges);
		Assert.assertNotNull(expenseCollection);
		Assert.assertTrue(expenseCollection.size() == 2);
	}

}
