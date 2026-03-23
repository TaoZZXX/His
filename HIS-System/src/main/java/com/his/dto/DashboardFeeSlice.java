package com.his.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardFeeSlice {
    private String name;
    private BigDecimal amount;
}
