/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.html5expense;

import com.springsource.html5expense.services.Flag;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "EXPENSE_REPORT")
public class ExpenseReport {

    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    @Id
    private Long id;

    private String purpose;

    @OneToMany(mappedBy = "expenseReport")
    private List<Expense> expenses = new ArrayList<Expense>();

    private BigDecimal receiptRequiredAmount = new BigDecimal("25.00");

    @Enumerated(EnumType.STRING)
    private State state = State.NEW;

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public State getState() {
        return this.state;
    }

    public ExpenseReport(String purpose) {
        this.purpose = purpose;
    }

    public ExpenseReport(Long id, String purpose) {
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

    public boolean isSubmitted() {
        return state == State.APPROVED || state == State.IN_REVIEW;
    }

    public Expense createExpense(EligibleCharge charge) {
        return this.createExpense(charge.getDate(), charge.getMerchant(),
                charge.getCategory(), charge.getAmount(), charge.getI());
    }

    private Expense createExpense(Date date, String merchant, String category, BigDecimal amount, Long chargeId) {
        assertOpen();
        Expense expense = new Expense(this, date, merchant, category, amount, chargeId);
        if (expense.getAmount().compareTo(receiptRequiredAmount) == 1)
            expense.flag("receiptRequired");
        this.expenses.add(expense);
        return expense;
    }

    public void attachReceipt(Integer expenseId, String receipt, String key) {
        assertOpen();
        getExpense(expenseId).attachReceipt(receipt, key);
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
        for (Expense expense : expenses) {
            if (expense.isFlagged()) {
                return true;
            }
        }
        return false;
    }

    private Expense getExpense(Integer id) {
        for (Expense expense : expenses) {
            if (expense.getId().equals(id)) {
                return expense;
            }
        }
        throw new IllegalArgumentException("No such expense");
    }

    // Hibernate

    ExpenseReport() {
    }

}