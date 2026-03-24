package com.his.service.impl;

import com.his.domain.Result;
import com.his.dto.DashboardFeeSlice;
import com.his.dto.DashboardVisitPoint;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.mapper.HomeDashboardMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.service.IHomeDashboardService;
import com.his.utils.JwtUtil;
import com.his.vo.DeptFeeBarChartVo;
import com.his.vo.DeptFeeSeriesVo;
import com.his.vo.HomeDashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeDashboardService implements IHomeDashboardService {

    private static final int CANCELED = RegistrationStatusCode.CANCELED.getCode();
    private static final int MAX_DEPTS_ON_BAR = 8;
    private static final List<Integer> ITEM_TYPES = Arrays.asList(1, 2, 3, 4, 5);
    private static final DateTimeFormatter PAY_RANGE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HomeDashboardMapper homeDashboardMapper;

    @Autowired
    private SmsStaffMapper smsStaffMapper;

    @Override
    public Result<HomeDashboardVo> loadDashboard(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (!jwtUtil.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        Long staffId = jwtUtil.getUserIdFromToken(token);
        if (staffId == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }

        LocalDate today = LocalDate.now();
        LocalDate start7 = today.minusDays(6);

        Long deptId = resolveDeptId(staffId);

        HomeDashboardVo vo = new HomeDashboardVo();
        vo.setPersonalTodayCount(homeDashboardMapper.countPersonalRegistrationsOnDate(staffId, today, CANCELED));
        vo.setDeptTodayCount(deptId == null ? 0L : homeDashboardMapper.countDeptRegistrationsOnDate(deptId, today, CANCELED));
        vo.setHospitalTodayCount(homeDashboardMapper.countHospitalRegistrationsOnDate(today, CANCELED));

        vo.setHospitalVisitLast7Days(buildVisitSeries(start7, today));
        vo.setFeeByCategory7Days(buildFeeByCategory(start7, today));
        vo.setDeptFeeBar7Days(buildDeptFeeBar(start7, today));

        return Result.success("查询成功", vo);
    }

    private Long resolveDeptId(Long staffId) {
        try {
            Integer id = smsStaffMapper.selectDeptIdById(staffId);
            return id == null ? null : id.longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private List<DashboardVisitPoint> buildVisitSeries(LocalDate start, LocalDate end) {
        List<Map<String, Object>> rows = homeDashboardMapper.selectHospitalRegistrationCountByDay(start, end, CANCELED);
        Map<LocalDate, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object dt = row.get("dt");
            Object cnt = row.get("cnt");
            if (dt == null || cnt == null) {
                continue;
            }
            LocalDate d;
            if (dt instanceof java.sql.Date) {
                d = ((java.sql.Date) dt).toLocalDate();
            } else if (dt instanceof LocalDate) {
                d = (LocalDate) dt;
            } else {
                d = LocalDate.parse(dt.toString().substring(0, 10));
            }
            long c = ((Number) cnt).longValue();
            map.put(d, c);
        }
        List<DashboardVisitPoint> list = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DashboardVisitPoint p = new DashboardVisitPoint();
            p.setDate(d.toString());
            p.setValue(map.getOrDefault(d, 0L));
            list.add(p);
        }
        return list;
    }

    private String payStart(LocalDate start) {
        return start.atStartOfDay().format(PAY_RANGE);
    }

    private String payEndExclusive(LocalDate endInclusive) {
        return endInclusive.plusDays(1).atStartOfDay().format(PAY_RANGE);
    }

    private List<DashboardFeeSlice> buildFeeByCategory(LocalDate start, LocalDate end) {
        String ps = payStart(start);
        String pe = payEndExclusive(end);
        List<Map<String, Object>> rows = homeDashboardMapper.selectFeeByItemTypeInPayTimeRange(ps, pe);
        Map<Integer, BigDecimal> byType = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object t = row.get("itemType");
            Object total = row.get("total");
            if (t == null || total == null) {
                continue;
            }
            int type = ((Number) t).intValue();
            BigDecimal amt = total instanceof BigDecimal ? (BigDecimal) total : new BigDecimal(total.toString());
            byType.merge(type, amt, BigDecimal::add);
        }
        List<DashboardFeeSlice> slices = new ArrayList<>();
        for (Integer type : ITEM_TYPES) {
            BigDecimal amt = byType.getOrDefault(type, BigDecimal.ZERO);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            DashboardFeeSlice s = new DashboardFeeSlice();
            s.setName(itemTypeLabel(type));
            s.setAmount(amt);
            slices.add(s);
        }
        return slices;
    }

    private DeptFeeBarChartVo buildDeptFeeBar(LocalDate start, LocalDate end) {
        String ps = payStart(start);
        String pe = payEndExclusive(end);
        List<Map<String, Object>> rows = homeDashboardMapper.selectDeptItemTypeFeeInPayTimeRange(ps, pe);

        Map<Long, String> deptNames = new LinkedHashMap<>();
        Map<Long, Map<Integer, BigDecimal>> cell = new HashMap<>();
        Map<Long, BigDecimal> deptTotals = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Object did = row.get("deptId");
            Object name = row.get("deptName");
            Object t = row.get("itemType");
            Object total = row.get("total");
            if (did == null || name == null || t == null || total == null) {
                continue;
            }
            long deptId = ((Number) did).longValue();
            deptNames.putIfAbsent(deptId, name.toString());
            int type = ((Number) t).intValue();
            BigDecimal amt = total instanceof BigDecimal ? (BigDecimal) total : new BigDecimal(total.toString());
            cell.computeIfAbsent(deptId, k -> new HashMap<>()).merge(type, amt, BigDecimal::add);
            deptTotals.merge(deptId, amt, BigDecimal::add);
        }

        List<Long> topDeptIds = deptTotals.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(MAX_DEPTS_ON_BAR)
                .collect(Collectors.toList());

        DeptFeeBarChartVo bar = new DeptFeeBarChartVo();
        for (Long id : topDeptIds) {
            bar.getCategories().add(deptNames.getOrDefault(id, String.valueOf(id)));
        }
        for (Integer type : ITEM_TYPES) {
            DeptFeeSeriesVo series = new DeptFeeSeriesVo();
            series.setName(itemTypeLabel(type));
            for (Long id : topDeptIds) {
                BigDecimal v = cell.getOrDefault(id, Collections.<Integer, BigDecimal>emptyMap()).getOrDefault(type, BigDecimal.ZERO);
                series.getData().add(v);
            }
            boolean any = series.getData().stream().anyMatch(x -> x != null && x.compareTo(BigDecimal.ZERO) > 0);
            if (any) {
                bar.getSeries().add(series);
            }
        }
        return bar;
    }

    private static String itemTypeLabel(int type) {
        switch (type) {
            case 1:
                return "非药品(检查检验)";
            case 2:
                return "成药费";
            case 3:
                return "草药费";
            case 4:
                return "挂号费";
            case 5:
                return "其它";
            default:
                return "类型" + type;
        }
    }
}
