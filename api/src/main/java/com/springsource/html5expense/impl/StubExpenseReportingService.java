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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.LocalDate;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReport;
import com.springsource.html5expense.ExpenseReportingService;

public class StubExpenseReportingService implements ExpenseReportingService {

    private final AtomicLong reportSequence = new AtomicLong();

    private final AtomicInteger expenseSequence = new AtomicInteger();

    private final Map<Long, EligibleCharge> eligibleCharges = new HashMap<Long, EligibleCharge>();
    
    private final Map<Long, ExpenseReportImpl> reports = new HashMap<Long, ExpenseReportImpl>();
    
    public StubExpenseReportingService() {
        eligibleCharges.put(1L, new EligibleCharge(1L, new LocalDate(2011, 7, 31), "Delta", "Air Travel", new BigDecimal("431.00")));
        eligibleCharges.put(2L, new EligibleCharge(2L, new LocalDate(2011, 8, 22), "Hilton", "Lodging", new BigDecimal("639.00")));        
        eligibleCharges.put(3L, new EligibleCharge(3L, new LocalDate(2011, 8, 22), "Chipotle", "Meals", new BigDecimal("24.00")));        
    }
    
    public Long createReport(String purpose) {
        ExpenseReportImpl report = new ExpenseReportImpl(reportSequence.incrementAndGet(), purpose);
        reports.put(report.getId(), report);
        return report.getId();
    }

    public List<EligibleCharge> getEligibleCharges() {
        return Collections.unmodifiableList(new ArrayList<EligibleCharge>(eligibleCharges.values()));
    }

    public List<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReportImpl report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();
        for (Long chargeId : chargeIds) {
            EligibleCharge charge = eligibleCharges.get(chargeId);
            Expense expense = report.createExpense(expenseSequence.incrementAndGet(), charge);
            expenses.add(expense);
            eligibleCharges.remove(chargeId);
        }
        return expenses;
    }

    public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
        String receiptReference = receiptReference(receiptBytes);
        ExpenseReportImpl report = getReport(reportId);
        report.attachReceipt(expenseId, receiptReference);
        return receiptReference;
    }

    public void submitReport(Long reportId) {
        ExpenseReportImpl report = getReport(reportId);
        report.markInReview();
    }

    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReport> openReports = new ArrayList<ExpenseReport>();
        for (ExpenseReportImpl report : reports.values()) {
            if (report.isOpen()) {
                openReports.add(report.dto());
            }
        }
        return openReports;
    }
    
    // expense review only (not part of reporting interface)
    
    public void reject(Long reportId, List<Flag> flags) {
        ExpenseReportImpl report = getReport(reportId);
        report.markRejected(flags);
    }

    public void approve(Long reportId) {
        ExpenseReportImpl report = getReport(reportId);
        report.markApproved();
    }

    // helpers

    private ExpenseReportImpl getReport(Long reportId) {
        return reports.get(reportId);
    }

    private String receiptReference(byte[] receiptBytes) {
        return "receiptReference";
    }
    
    static enum State {
        NEW, IN_REVIEW, REJECTED, APPROVED;
    }
    
    static class ExpenseReportImpl {

        private Long id;
        
        private String purpose;

        private List<ExpenseImpl> expenses = new ArrayList<ExpenseImpl>();

        private State state = State.NEW; 
        
        private BigDecimal receiptRequiredAmount = new BigDecimal("25.00");
        
        public ExpenseReportImpl(Long id, String purpose) {
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

        public Expense createExpense(Integer expenseId, EligibleCharge charge) {
            assertOpen();
            ExpenseImpl expense = new ExpenseImpl(expenseId, charge);
            if (charge.getAmount().compareTo(receiptRequiredAmount) == 1) {
                expense.flag("receiptRequired");
            }            
            expenses.add(expense);
            return expense.dto();
        }
        
        public void attachReceipt(Integer expenseId, String receipt) {
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
        
        public ExpenseReport dto() {
            List<Expense> expenses = new ArrayList<Expense>();
            for (ExpenseImpl expense : this.expenses) {
                expenses.add(expense.dto());
            }
            return new ExpenseReport(id, purpose, expenses);
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
            for (ExpenseImpl expense : expenses) {
                if (expense.isFlagged()) {
                    return true;
                }
            }
            return false;
        }
        
        private ExpenseImpl getExpense(Integer id) {
            for (ExpenseImpl expense : expenses) {
                if (expense.getId().equals(id)) {
                    return expense;
                }
            }
            throw new IllegalArgumentException("No such expense");
        }
        
    }
    
   static class ExpenseImpl {

        private Integer id;
        
        private LocalDate date;
        
        private String merchant;
        
        private String category;
        
        private BigDecimal amount;
        
        private Long chargeId;
        
        private String receipt;
        
        private String flag;
        
        public ExpenseImpl(Integer id, EligibleCharge charge) {
           this.id = id;
           this.date = charge.getDate();
           this.merchant = charge.getMerchant();
           this.category = charge.getCategory();
           this.amount = charge.getAmount();
           this.chargeId = charge.getId();
        }

        public Integer getId() {
            return id;
        }

        public boolean isFlagged() {
            return flag != null;
        }
        
        public void flag(String flag) {
            this.flag = flag;
        }

        public void attachReceipt(String receipt) {
            this.receipt = receipt;
            this.flag = null;
        }
        
        public Expense dto() {
            return new Expense(id, date, merchant, category, amount, chargeId, receipt, flag);
        }
        
    }
   
   public static class Flag {
       
       private final Integer expenseId;
       
       private final String value;

       public Flag(Integer expenseId, String value) {
        this.expenseId = expenseId;
        this.value = value;
       }

       public Integer getExpenseId() {
           return expenseId;
       }

       public String getValue() {
           return value;
       }
       
   }

    
}