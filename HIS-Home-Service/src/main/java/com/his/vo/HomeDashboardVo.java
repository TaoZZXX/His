package com.his.vo;

import com.his.dto.DashboardFeeSlice;
import com.his.dto.DashboardVisitPoint;

import java.util.ArrayList;
import java.util.List;

public class HomeDashboardVo {
    private long personalTodayCount;
    private long deptTodayCount;
    private long hospitalTodayCount;
    private List<DashboardVisitPoint> hospitalVisitLast7Days = new ArrayList<>();
    private List<DashboardFeeSlice> feeByCategory7Days = new ArrayList<>();
    private DeptFeeBarChartVo deptFeeBar7Days = new DeptFeeBarChartVo();

    public long getPersonalTodayCount() { return personalTodayCount; }
    public void setPersonalTodayCount(long personalTodayCount) { this.personalTodayCount = personalTodayCount; }
    public long getDeptTodayCount() { return deptTodayCount; }
    public void setDeptTodayCount(long deptTodayCount) { this.deptTodayCount = deptTodayCount; }
    public long getHospitalTodayCount() { return hospitalTodayCount; }
    public void setHospitalTodayCount(long hospitalTodayCount) { this.hospitalTodayCount = hospitalTodayCount; }
    public List<DashboardVisitPoint> getHospitalVisitLast7Days() { return hospitalVisitLast7Days; }
    public void setHospitalVisitLast7Days(List<DashboardVisitPoint> hospitalVisitLast7Days) { this.hospitalVisitLast7Days = hospitalVisitLast7Days; }
    public List<DashboardFeeSlice> getFeeByCategory7Days() { return feeByCategory7Days; }
    public void setFeeByCategory7Days(List<DashboardFeeSlice> feeByCategory7Days) { this.feeByCategory7Days = feeByCategory7Days; }
    public DeptFeeBarChartVo getDeptFeeBar7Days() { return deptFeeBar7Days; }
    public void setDeptFeeBar7Days(DeptFeeBarChartVo deptFeeBar7Days) { this.deptFeeBar7Days = deptFeeBar7Days; }
}
