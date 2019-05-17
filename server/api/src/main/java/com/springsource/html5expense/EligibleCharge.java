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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Simple entity to describe a charge that has yet to be reconciled.
 */
@Entity
@Table(name = "ELIGIBLE_CHARGE")
public class EligibleCharge {

    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    @Id
    private Long id;

    public Long getId() {
        return this.id;
    }

    private String merchant, category;
    private Date charge_date;
    private BigDecimal amount;

    public EligibleCharge() {
    }

    public EligibleCharge(Date charge_date, String merchant, String category, BigDecimal bigDecimal) {
        this.merchant = merchant;
        this.category = category;
        this.charge_date = charge_date;
        this.amount = bigDecimal;
    }

    public Date getDate() {
        return charge_date;
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

    public Long getI() {
        return this.id;
    }
}
