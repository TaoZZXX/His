package com.his.dto;

import java.math.BigDecimal;

public class DashboardFeeSlice {
    private String name;
    private BigDecimal amount;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
