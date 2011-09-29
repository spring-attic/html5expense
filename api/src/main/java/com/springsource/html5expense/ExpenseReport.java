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

import java.util.List;

public class ExpenseReport {

    private Long id;

    private String purpose;

    private State state;

    private List<Expense> expenses;

    private ExpenseReport() {
    }

    public ExpenseReport(Long id, String purpose, State state, List<Expense> expenses) {
        this.id = id;
        this.state = state;
        this.purpose = purpose;
        this.expenses = expenses;
    }

    public State getState() {
        return state;
    }

    public Long getId() {
        return id;
    }

    public String getPurpose() {
        return purpose;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    private void setState(State state) {
        this.state = state;
    }

    private void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @Override
    public String toString() {
        return "ExpenseReport{" +
                       "id=" + id +
                       ", purpose='" + purpose + '\'' +
                       ", state=" + state +
                       ", expenses=" + expenses +
                       '}';
    }
}
