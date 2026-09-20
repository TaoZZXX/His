package com.his.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeptFeeSeriesVo {
    private String name;
    private List<BigDecimal> data = new ArrayList<>();
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<BigDecimal> getData() { return data; }
    public void setData(List<BigDecimal> data) { this.data = data; }
}
