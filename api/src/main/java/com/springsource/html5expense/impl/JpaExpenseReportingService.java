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

import com.springsource.html5expense.*;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Josh Long
 */
@Service
public class JpaExpenseReportingService implements ExpenseReportingService {
    @Inject private DataSource dataSource;

    @PersistenceContext private EntityManager entityManager;

    private JdbcTemplate jdbcTemplate;

    /**
     * {@link RowMapper} that knows how to map a {@link ResultSet} int a {@link EligibleCharge}.
     *
     * Not all things map perfectly onto JPA, thankfully Spring makes it easy to work with JDBC, too.
     *
     */
    private RowMapper<EligibleCharge> eligibleChargeRowMapper = new RowMapper<EligibleCharge>() {
        public EligibleCharge mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EligibleCharge(rs.getLong("ID"),
                                             new LocalDate(rs.getDate("DATE")),
                                             rs.getString("MERCHANT"),
                                             rs.getString("CATEGORY"),
                                             new BigDecimal(rs.getDouble("AMOUNT")));
        }
    };

    @PostConstruct
    public void construct() throws Exception {
        jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Transactional
    public Long createReport(String purpose) {
        ExpenseReportEntity report = new ExpenseReportEntity(purpose);
        entityManager.persist(report);
        return report.getId();
    }

    @Transactional(readOnly = true)
    public Collection<EligibleCharge> getEligibleCharges() {
        return jdbcTemplate.query("SELECT * FROM ELIGIBLE_CHARGE", eligibleChargeRowMapper);
    }

    @Transactional
    public Collection<Expense> createExpenses(Long reportId, List<Long> chargeIds) {
        ExpenseReportEntity report = getReport(reportId);
        List<Expense> expenses = new ArrayList<Expense>();

        // cache all the charges for this operation
        List<EligibleCharge> charges = getEligibleCharges(chargeIds.toArray(new Long[chargeIds.size()]));

        Map<Long, EligibleCharge> chargeMap = new HashMap<Long, EligibleCharge>();
        for (EligibleCharge ec : charges) {
            chargeMap.put(ec.getId(), ec);
        }

        for (Long chargeId : chargeIds) {

            ExpenseEntity expense = report.createExpense(chargeMap.get(chargeId));

            entityManager.persist(expense);
            expenses.add(expense.data());
        }

        deleteCharges(chargeIds);

        return expenses;
    }

    @Transactional
    public String attachReceipt(Long reportId, Integer expenseId, byte[] receiptBytes) {
        ExpenseReportEntity report = getReport(reportId);
        String receipt = receipt(receiptBytes);
        report.attachReceipt(expenseId, receipt);
        entityManager.merge(report);
        return receipt;
    }

    @Transactional
    public void submitReport(Long reportId) {
        ExpenseReportEntity entity = getReport(reportId);
        entity.markInReview();
        entityManager.merge(entity);
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> getOpenReports() {
        List<ExpenseReportEntity> entities = entityManager.createQuery(
          "SELECT em FROM com.springsource.html5expense.impl.ExpenseReportEntity em WHERE em.state = :state", ExpenseReportEntity.class)
         .setParameter("state", State.NEW).getResultList();

        List<ExpenseReport> reports = new ArrayList<ExpenseReport>();
        for (ExpenseReportEntity er : entities) {
            reports.add(er.data());
        }
        return reports;
    }

    protected ExpenseReportEntity getReport(Long reportId) {
        return entityManager.find(ExpenseReportEntity.class, reportId);
    }

    @Transactional(readOnly = true)
    protected List<EligibleCharge> getEligibleCharges(final Long[] ecIds) {
        return jdbcTemplate.query(" SELECT * FROM ELIGIBLE_CHARGE WHERE ID IN( " + StringUtils.arrayToDelimitedString(ecIds, ",") + " ) ", eligibleChargeRowMapper);
    }

    //TODO !!! grab the relevant package from greenhouse
    protected String receipt(byte[] receiptBytes) {
        return "receipt for bytes";
    }

    protected void deleteCharges(List<Long> chargeIds){
       jdbcTemplate.execute(" DELETE FROM ELIGIBLE_CHARGE WHERE ID IN( " + StringUtils.arrayToDelimitedString( chargeIds.toArray( new Long[chargeIds.size()]), ",") + " ) " );
    }
}