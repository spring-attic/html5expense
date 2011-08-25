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

public class EligibleCharge {

    private final Long id;

    private final LocalDate date;

    private final String merchant;

    private final String category;

    private final BigDecimal amount;

    public EligibleCharge(Long id, LocalDate date, String merchant, String category, BigDecimal amount) {
        this.id = id;
        this.date = date;
        this.merchant = merchant;
        this.category = category;
        this.amount = amount;
    }

    public Long getId() {
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

}
