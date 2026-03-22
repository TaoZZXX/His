package com.his.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeptFeeSeriesVo {
    private String name;
    private List<BigDecimal> data = new ArrayList<>();
}
