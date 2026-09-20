package com.his.service.impl;

import com.his.domain.Result;
import com.his.dto.DateRangeDto;
import com.his.dto.SkdRuleCreateDto;
import com.his.enums.NoonCode;
import com.his.enums.ResultCode;
import com.his.mapper.SkdRuleMapper;
import com.his.service.ISkdRuleService;
import com.his.vo.SkdRuleDetailVo;
import com.his.vo.SkdRuleItemVo;
import com.his.vo.SkdRuleVo;
import com.his.vo.IdResponseVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SkdRuleService implements ISkdRuleService {
    private final SkdRuleMapper skdRuleMapper;
    public SkdRuleService(SkdRuleMapper skdRuleMapper) { this.skdRuleMapper = skdRuleMapper; }

    @Override
    @Transactional
    public Result<IdResponseVo> createRule(SkdRuleCreateDto dto) {
        if (dto == null) return paramError("规则参数不能为空");
        Long deptId = dto.getDeptId();
        String ruleName = dto.getRuleName();
        if (deptId == null || ruleName == null || ruleName.trim().isEmpty()) return paramError("规则参数不能为空");
        Map<String, Object> row = new HashMap<>();
        row.put("status", 0); row.put("operatorId", 0L); row.put("operateTime", LocalDateTime.now()); row.put("ruleName", ruleName); row.put("description", dto.getRuleDesc()); row.put("deptId", deptId);
        skdRuleMapper.insertRule(row);
        Long ruleId = ((Number) row.get("id")).longValue();
        List<SkdRuleCreateDto.SkdRuleItemDto> itemsObj = dto.getItems();
        if (itemsObj != null) for (SkdRuleCreateDto.SkdRuleItemDto it : itemsObj) {
            Long doctorId = it == null ? null : it.getDoctorId();
            Long limit = it == null ? null : it.getLimit();
            if (doctorId == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("staffId", doctorId); item.put("daysOfWeek", encodeDays(it == null ? null : it.getSkd())); item.put("status", 0); item.put("skLimit", limit == null ? 0 : limit); item.put("skRuleId", ruleId);
            skdRuleMapper.insertRuleItem(item);
        }
        return Result.success("新增排班规则成功", new IdResponseVo(ruleId));
    }

    @Override public Result<List<SkdRuleVo>> listRules(Long deptId) { return Result.success("查询成功", skdRuleMapper.selectRules(deptId)); }
    @Override public Result<SkdRuleDetailVo> getRuleDetail(Long id) { SkdRuleDetailVo d = new SkdRuleDetailVo(); List<SkdRuleVo> r = skdRuleMapper.selectRuleById(id); d.setRule(r.isEmpty() ? null : r.get(0)); List<SkdRuleItemVo> items = skdRuleMapper.selectRuleItems(id); d.setItems(items); return Result.success("查询成功", d); }
    @Override public Result<Object> publish(Long id) { skdRuleMapper.publishRule(id); return Result.success("发布成功"); }

    @Override
    @Transactional
    public Result<Object> generate(Long id, DateRangeDto body) {
        if (id == null || body == null || body.getStartDate() == null || body.getEndDate() == null) return paramError("规则ID和日期不能为空");
        LocalDate start = LocalDate.parse(body.getStartDate()), end = LocalDate.parse(body.getEndDate());
        List<SkdRuleVo> rules = skdRuleMapper.selectRuleById(id);
        if (rules.isEmpty()) return paramError("规则不存在");
        Long deptId = rules.get(0).getDeptId();
        List<SkdRuleItemVo> items = skdRuleMapper.selectRuleItems(id);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String dk = dayKey(d);
            for (SkdRuleItemVo it : items) {
                Set<String> ds = parseDays(it.getDaysOfWeek());
                Long staffId = it.getStaffId(), limit = it.getSkLimit();
                if (ds.contains(dk + "_am")) skdRuleMapper.insertSkd(d.atTime(9, 0), 1, limit, NoonCode.MORNING.getCode(), staffId, deptId, String.valueOf(limit));
                if (ds.contains(dk + "_pm")) skdRuleMapper.insertSkd(d.atTime(15, 0), 1, limit, NoonCode.AFTERNOON.getCode(), staffId, deptId, String.valueOf(limit));
            }
        }
        return Result.success("生成排班计划表成功");
    }

    private static String encodeDays(Object skdObj) {
        if (!(skdObj instanceof Map)) return "";
        List<String> parts = new ArrayList<>();
        Map<?, ?> skd = (Map<?, ?>) skdObj;
        for (Map.Entry<?, ?> e : skd.entrySet()) {
            String day = String.valueOf(e.getKey());
            if (!(e.getValue() instanceof Map)) continue;
            Map<?, ?> v = (Map<?, ?>) e.getValue();
            if (Boolean.TRUE.equals(v.get("am"))) parts.add(day + "_am");
            if (Boolean.TRUE.equals(v.get("pm"))) parts.add(day + "_pm");
        }
        return String.join(",", parts);
    }
    private static Set<String> parseDays(String text) { Set<String> s = new HashSet<>(); if (text == null || text.isEmpty()) return s; for (String p : text.split(",")) if (!p.trim().isEmpty()) s.add(p.trim()); return s; }
    private static String dayKey(LocalDate d) { switch (d.getDayOfWeek()) { case MONDAY: return "mon"; case TUESDAY: return "tue"; case WEDNESDAY: return "wed"; case THURSDAY: return "thu"; case FRIDAY: return "fri"; case SATURDAY: return "sat"; default: return "sun"; } }
    private static String asString(Object o) { return o == null ? null : String.valueOf(o); }
    private static Long asLong(Object o) { if (o == null) return null; if (o instanceof Number) return ((Number) o).longValue(); try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; } }
    private <T> Result<T> paramError(String message) { return Result.error(ResultCode.PARAM_ERROR, message); }
}
