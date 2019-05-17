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


import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "EXPENSE")
public class Expense {

    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    @Id
    private Integer id;

    @ManyToOne
    private ExpenseReport expenseReport;

    private Date expense_date;

    private String merchant;

    private String category;

    private BigDecimal amount;

    private Long chargeId;

    private String receipt;

    private String receiptExtension;

    private String flag;


    Expense(ExpenseReport er, Date expense_date, String merchant, String category, BigDecimal amount, Long chargeId) {
        this.expense_date = expense_date;
        this.expenseReport = er;
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
        this.chargeId = chargeId;
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

    public String getReceipt() {
        return receipt;
    }

    public String getReceiptExtension() {
        return this.receiptExtension;
    }

    public void attachReceipt(String receipt, String extension) {
        this.receipt = receipt;
        this.receiptExtension = extension;

        if (isFlagged() && this.flag.equals("receiptRequired")) {
            this.flag = null;
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public Date getDate() {
        return expense_date;
    }

    public ExpenseReport getExpenseReport() {
        return expenseReport;
    }

    public String getFlag() {
        return flag;
    }

    public String getMerchant() {
        return merchant;
    }
    // hibernate

    Expense() {
    }

}