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
package com.springsource.html5expense;

import org.joda.time.LocalDate;

import java.math.BigDecimal;

public class Expense {

    private Integer id;

    private LocalDate date;

    private String merchant;

    private String category;

    private BigDecimal amount;

    private Long chargeId;

    private String receipt;

    private String flag;

    private Expense() {
    }

    public Expense(Integer id, LocalDate date, String merchant, String category, BigDecimal amount, Long chargeId, String receipt, String flag) {
        this.id = id;
        this.date = date;
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
        this.chargeId = chargeId;
        this.receipt = receipt;
        this.flag = flag;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public String getReceipt() {
        return receipt;
    }

    public String getFlag() {
        return flag;
    }

    private void setId(Integer id) {
        this.id = id;
    }

    private  void setDate(LocalDate date) {
        this.date = date;
    }

    private  void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    private  void setCategory(String category) {
        this.category = category;
    }

    private  void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    private  void setChargeId(Long chargeId) {
        this.chargeId = chargeId;
    }

    private  void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    private  void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Expense{" +
                       "id=" + id +
                       ", date=" + date +
                       ", merchant='" + merchant + '\'' +
                       ", category='" + category + '\'' +
                       ", amount=" + amount +
                       ", chargeId=" + chargeId +
                       ", receipt='" + receipt + '\'' +
                       ", flag='" + flag + '\'' +
                       '}';
    }
}