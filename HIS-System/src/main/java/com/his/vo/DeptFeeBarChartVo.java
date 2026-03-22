package com.his.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeptFeeBarChartVo {
    private List<String> categories = new ArrayList<>();
    private List<DeptFeeSeriesVo> series = new ArrayList<>();
}
