package com.his.vo;

import com.his.dto.DashboardFeeSlice;
import com.his.dto.DashboardVisitPoint;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeDashboardVo {

    private long personalTodayCount;
    private long deptTodayCount;
    private long hospitalTodayCount;

    private List<DashboardVisitPoint> hospitalVisitLast7Days = new ArrayList<>();
    private List<DashboardFeeSlice> feeByCategory7Days = new ArrayList<>();
    private DeptFeeBarChartVo deptFeeBar7Days = new DeptFeeBarChartVo();
}
