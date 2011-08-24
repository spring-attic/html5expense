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
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the {@link ExpenseReportingService} that delegates to JPA to provide persistence
 *
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

	protected EligibleCharge getEligibleCharge(Long ecId) {
		EligibleChargeEntity ece = entityManager.find(EligibleChargeEntity.class, ecId);
		return new EligibleCharge(ece.getId(), new LocalDate(ece.getDate().getTime()), ece.getMerchant(), ece.getCategory(), ece.getAmount());
	}

	public Collection<EligibleCharge> getEligibleCharges() {
		return null;
	}

	@Transactional
	public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
		ExpenseReportEntity report = getReport(reportId);
		List<Expense> expenses = new ArrayList<Expense>();
		for (Long chargeId : chargeIds) {
			EligibleCharge charge = getEligibleCharge(chargeId);
			ExpenseEntity expense = report.createExpense(charge) ;
			entityManager.persist(expense);
			expenses.add( expense .data());
		}
		return expenses;
	}

	public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
		return null;
	}

	public void submitReport(Long reportId) {
	}

	public List<ExpenseReport> getOpenReports() {
		return null;
	}

	protected ExpenseReportEntity getReport(Long reportId) {
		return entityManager.find(ExpenseReportEntity.class, reportId);
	}
}
