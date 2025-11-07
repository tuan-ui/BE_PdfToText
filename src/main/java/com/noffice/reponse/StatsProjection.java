package com.noffice.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface StatsProjection {
    // contract statistic
    Long getCreditContractCount();
    Long getMortgageContractCount();
    // customer statistic
    Long getCustomersCount();
    Long getRestrictedCustomersCount();
    // credit statistic
    String getFullName();
    Long getCount();
    Long getUserId();
    // Debt structure
    String getContent();
    Long getGoalCount();
    // sum debt
    Long getLabel();
    Long getSum();
    // available capital
    Long getMonth();
    String getSummary();
}
