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
package com.springsource.html5expense.integrations;

/**
 * well known headers that need to be present when constructing
 * {@link com.springsource.html5expense.EligibleCharge}
 * contributions in routing code.
 *
 * @author Josh Long
 */
public class EligibleChargeProcessorHeaders {

    /**
     * Well known header for the date of the Eligible charge
     */
    static public final String EC_DATE = "ec_date";
    /**
     * well known header for the merchant
     */
    static public final String EC_MERCHANT = "ec_merchant";

    /**
     * well known header for the amount
     */
    static public final String EC_AMOUNT = "ec_amount";

    /**
     * well known header for the category
     */
    static public final String EC_CATEGORY = "ec_category";

}
