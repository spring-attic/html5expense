package com.springsource.html5expense.services;

import com.springsource.html5expense.EligibleCharge;
import com.springsource.html5expense.Expense;
import com.springsource.html5expense.ExpenseReportingService;
import com.springsource.html5expense.config.ComponentConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link JpaExpenseReportingService JPA expense reporting service}.
 *
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ComponentConfig.class})
@TransactionConfiguration(defaultRollback = true)
@Transactional
@ActiveProfiles("local")
public class TestJpaExpenseReportingService {

    @Inject
    private ExpenseReportingService service;

    private String purpose = "SFO face to face";

    @Test
    public void testCreatingAnExpenseReport() throws Throwable {
        Long reportId = this.service.createReport(this.purpose);
        assertNotNull(reportId);
        assertTrue(reportId > 0);
    }

    @Test
    public void testCreatingAnExpenseReportExpenses() throws Throwable {
        Long reportId = service.createReport(this.purpose);
        Collection<EligibleCharge> chargeCollection = this.service.getEligibleCharges();
        List<Long> ids = new ArrayList<Long>(chargeCollection.size());
        for (EligibleCharge eligibleCharge : chargeCollection)
            ids.add(eligibleCharge.getId());
        Collection<Expense> expenseCollection = service.createExpenses(reportId, ids);
        assertEquals(expenseCollection.size(), ids.size());
        assertEquals(expenseCollection.size(), service.getExpensesForExpenseReport(reportId).size());
    }
}
