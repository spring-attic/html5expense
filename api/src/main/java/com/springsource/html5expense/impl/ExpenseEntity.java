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

import org.joda.time.LocalDate;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;

class ExpenseEntity {

    private Integer id;

    private LocalDate date;

    private String merchant;

    private String category;

    private BigDecimal amount;

    private Long chargeId;

    private String receipt;

    private String flag;

    public ExpenseEntity(Integer id, EligibleCharge charge) {
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
        if (isFlagged() && this.flag.equals("receiptRequired")) {
            this.flag = null;
        }
    }

    public Expense data() {
        return new Expense(id, date, merchant, category, amount, chargeId, receipt, flag);
    }

}