package com.springsource.html5expense.integrations;

/**
 * well known headers that need to be present when constructing {@link com.springsource.html5expense.EligibleCharge}
 * contributions in routing code.
 *
 * @author Josh Long
 */
public class EligibleChargeProcessorHeaders {

    /**
     * Well known header for the date of the Eligible charge
     */
    static public final String EC_DATE= "ec_date";
    /**
     * well known header for the merchant
     */
    static public final String EC_MERCHANT = "ec_merchant";

    /**
     * well known header for the amount
     */
    static public final String EC_AMOUNT  = "ec_amount";

    /**
     * well known header for the category
     */
    static public final String EC_CATEGORY= "ec_category";

}
