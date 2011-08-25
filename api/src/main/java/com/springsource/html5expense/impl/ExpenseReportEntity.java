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
import com.springsource.html5expense.State;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EXPENSE_REPORT")
class ExpenseReportEntity {

	@GeneratedValue @Id
	private Long id;

	private String purpose;

	@OneToMany(mappedBy = "expenseReport")
	private List<ExpenseEntity> expenses = new ArrayList<ExpenseEntity>();

	@Enumerated(EnumType.STRING)
	private State state = State.NEW;

	private BigDecimal receiptRequiredAmount = new BigDecimal("25.00");

	/**
	 * hibernate
	 */
	ExpenseReportEntity() {
	}

	public ExpenseReportEntity(String purpose) {
		this.purpose = purpose;
	}

	public ExpenseReportEntity(Long id, String purpose) {
		this.id = id;
		this.purpose = purpose;
	}

	public Long getId() {
		return id;
	}

	public String getPurpose() {
		return purpose;
	}

	public boolean isOpen() {
		return state == State.NEW || state == State.REJECTED;
	}

	public ExpenseEntity createExpense(EligibleCharge charge) {
		assertOpen();
		ExpenseEntity expense = new ExpenseEntity(this, charge);
		if (charge.getAmount().compareTo(receiptRequiredAmount) == 1) {
			expense.flag("receiptRequired");
		}
		expenses.add(expense);
		return expense;
	}

	public void attachReceipt(Integer expenseId, String receipt) {
		assertOpen();
		getExpense(expenseId).attachReceipt(receipt);
	}

	public void markInReview() {
		assertOpen();
		if (isFlagged()) {
			throw new IllegalStateException("Report is flagged");
		}
		this.state = State.IN_REVIEW;
	}

	public void markRejected(List<Flag> flags) {
		assertInReview();
		for (Flag flag : flags) {
			getExpense(flag.getExpenseId()).flag(flag.getValue());
		}
		this.state = State.REJECTED;
	}

	public void markApproved() {
		assertInReview();
		this.state = State.APPROVED;
	}

	public ExpenseReport data() {
		List<Expense> expenses = new ArrayList<Expense>();
		for (ExpenseEntity expense : this.expenses) {
			expenses.add(expense.data());
		}
		return new ExpenseReport(id, purpose, this.state, expenses);
	}

	// internal helpers

	private void assertOpen() {
		if (!isOpen()) {
			throw new IllegalStateException("Report not open");
		}
	}

	private void assertInReview() {
		if (state != State.IN_REVIEW) {
			throw new IllegalStateException("Report not in review");
		}
	}

	private boolean isFlagged() {
		for (ExpenseEntity expense : expenses) {
			if (expense.isFlagged()) {
				return true;
			}
		}
		return false;
	}

	private ExpenseEntity getExpense(Integer id) {
		for (ExpenseEntity expense : expenses) {
			if (expense.getId().equals(id)) {
				return expense;
			}
		}
		throw new IllegalArgumentException("No such expense");
	}

}