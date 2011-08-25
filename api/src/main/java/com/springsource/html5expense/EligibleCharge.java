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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class EligibleCharge {

    @GeneratedValue @Id
    private Long id;

    private Date date;

    private String merchant;

    private String category;

    private BigDecimal amount;

    EligibleCharge() {
    }

    public EligibleCharge(LocalDate date, String merchant, String category, BigDecimal amount) {
        this.date = date.toDate();
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
    }

    public EligibleCharge(Long id, LocalDate date, String merchant, String category, BigDecimal amount) {
        this.id = id;
        this.date = date.toDate();
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return new LocalDate(date);
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

}
