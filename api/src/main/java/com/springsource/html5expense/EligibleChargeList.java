package com.springsource.html5expense;

import java.util.List;

public class EligibleChargeList {

    private List<Long> chargeIds;

    public List<Long> getChargeIds() {
        return chargeIds;
    }

    public void setChargeIds(List<Long> chargeIds) {
        this.chargeIds = chargeIds;
    }

    public EligibleChargeList() {
    }

    public EligibleChargeList(List<Long> ids) {
        this.chargeIds = ids;
    }
}
