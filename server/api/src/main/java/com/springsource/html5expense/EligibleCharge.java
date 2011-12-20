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

    @GeneratedValue
    @Id
    private Long id;

    public Long getId() {
        return this.id;
    }

    private String merchant, category;
    private Date date;
    private BigDecimal amount;

    public EligibleCharge() {
    }

    public EligibleCharge(Date date, String merchant, String category, BigDecimal bigDecimal) {
        this.merchant = merchant;
        this.category = category;
        this.date = date;
        this.amount = bigDecimal;
    }

    public Date getDate() {
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

    public Long getI() {
        return this.id;
    }
}
