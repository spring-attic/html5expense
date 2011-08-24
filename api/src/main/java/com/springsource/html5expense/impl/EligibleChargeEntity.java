package com.springsource.html5expense.impl;

import org.joda.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ELIGIBLE_CHARGE")
class EligibleChargeEntity {

	@Id @GeneratedValue
	private Long id;

	private Date date;

	private String merchant;

	private String category;

	private BigDecimal amount;

	EligibleChargeEntity() {
	}

	public EligibleChargeEntity(LocalDate date, String merchant, String category, BigDecimal amount) {
		this.date = date.toDate();
		this.merchant = merchant;
		this.category = category;
		this.amount = amount;
	}

	public Long getId() {
		return id;
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
}
