package com.his.vo;

import java.util.ArrayList;
import java.util.List;

public class DeptFeeBarChartVo {
    private List<String> categories = new ArrayList<>();
    private List<DeptFeeSeriesVo> series = new ArrayList<>();
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public List<DeptFeeSeriesVo> getSeries() { return series; }
    public void setSeries(List<DeptFeeSeriesVo> series) { this.series = series; }
}
