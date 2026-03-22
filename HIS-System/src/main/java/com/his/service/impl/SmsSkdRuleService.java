package com.his.service.impl;

import com.his.domain.SmsSkd;
import com.his.domain.SmsSkdRuleItem;
import com.his.domain.SmsSkdRule;
import com.his.dto.SkdRuleCreateDto;
import com.his.enums.ResultCode;
import com.his.enums.NoonCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsSkdMapper;
import com.his.mapper.SmsSkdRuleItemMapper;
import com.his.mapper.SmsSkdRuleMapper;
import com.his.service.ISmsSkdRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SmsSkdRuleService implements ISmsSkdRuleService {

    @Autowired
    private SmsSkdRuleMapper smsSkdRuleMapper;

    @Autowired
    private SmsSkdRuleItemMapper smsSkdRuleItemMapper;

    @Autowired
    private SmsSkdMapper smsSkdMapper;

    private String encodeDaysOfWeek(Map<String, Map<String, Boolean>> skd) {
        if (skd == null || skd.isEmpty()) return "";
        // 格式：mon_am,mon_pm,tue_am...
        List<String> parts = new ArrayList<>();
        for (String day : skd.keySet()) {
            Map<String, Boolean> v = skd.get(day);
            if (v == null) continue;
            Boolean am = v.get("am");
            Boolean pm = v.get("pm");
            if (Boolean.TRUE.equals(am)) parts.add(day + "_am");
            if (Boolean.TRUE.equals(pm)) parts.add(day + "_pm");
        }
        return String.join(",", parts);
    }

    @Override
    @Transactional
    public Long createRule(SkdRuleCreateDto dto) {
        if (dto == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "规则参数不能为空");
        }
        if (dto.getDeptId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室ID不能为空");
        }
        if (dto.getRuleName() == null || dto.getRuleName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "规则名称不能为空");
        }

        SmsSkdRule rule = new SmsSkdRule();
        rule.setStatus(0); // 0:草稿/未发布
        rule.setOperatorId(0L); // TODO: 接入登录后取真实操作者
        rule.setOperateTime(LocalDateTime.now());
        rule.setRuleName(dto.getRuleName());
        rule.setDescription(dto.getRuleDesc());
        rule.setDeptId(dto.getDeptId());
        Integer r = smsSkdRuleMapper.insert(rule);
        if (r == null || r <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "新增排班规则失败");
        }
        Long ruleId = rule.getId();

        List<SkdRuleCreateDto.SkdRuleItemDto> items = dto.getItems();
        if (items == null || items.isEmpty()) {
            return ruleId;
        }

        List<SmsSkdRuleItem> rows = new ArrayList<>();
        for (SkdRuleCreateDto.SkdRuleItemDto it : items) {
            if (it == null || it.getDoctorId() == null) continue;
            SmsSkdRuleItem row = new SmsSkdRuleItem();
            row.setSkRuleId(ruleId);
            row.setStaffId(it.getDoctorId());
            row.setSkLimit(it.getLimit() == null ? 0L : it.getLimit());
            row.setStatus(0);
            row.setDaysOfWeek(encodeDaysOfWeek(it.getSkd()));
            rows.add(row);
        }
        if (!rows.isEmpty()) {
            smsSkdRuleItemMapper.insertBatch(rows);
        }
        return ruleId;
    }

    @Override
    public SmsSkdRule getRule(Long id) {
        if (id == null) return null;
        return smsSkdRuleMapper.selectById(id);
    }

    @Override
    public List<SmsSkdRule> listRulesByDept(Long deptId) {
        if (deptId == null) return new ArrayList<>();
        return smsSkdRuleMapper.selectByDeptId(deptId);
    }

    @Override
    public List<SmsSkdRuleItem> listRuleItems(Long ruleId) {
        if (ruleId == null) return new ArrayList<>();
        return smsSkdRuleItemMapper.selectByRuleId(ruleId);
    }

    @Override
    public void publishRule(Long ruleId) {
        if (ruleId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "规则ID不能为空");
        }
        Integer r = smsSkdRuleMapper.updateStatus(ruleId, 1);
        if (r == null || r <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "发布失败");
        }
        // 发布规则：目前仅更新状态，具体生成排班由 generateSkdByRule 负责
    }

    private Set<String> parseDays(String daysOfWeek) {
        Set<String> set = new HashSet<>();
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return set;
        }
        String[] parts = daysOfWeek.split(",");
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                set.add(p.trim());
            }
        }
        return set;
    }

    private String dayKey(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "mon";
            case TUESDAY:
                return "tue";
            case WEDNESDAY:
                return "wed";
            case THURSDAY:
                return "thu";
            case FRIDAY:
                return "fri";
            case SATURDAY:
                return "sat";
            case SUNDAY:
                return "sun";
            default:
                return "";
        }
    }

    @Override
    @Transactional
    public void generateSkdByRule(Long ruleId, String startDate, String endDate) {
        if (ruleId == null || startDate == null || endDate == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "规则ID和日期不能为空");
        }
        SmsSkdRule rule = smsSkdRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "排班规则不存在");
        }
        List<SmsSkdRuleItem> items = smsSkdRuleItemMapper.selectByRuleId(ruleId);
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "规则明细为空，无法生成排班");
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (end.isBefore(start)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "结束日期不能早于开始日期");
        }

        List<SmsSkd> list = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String dk = dayKey(d);
            if (dk.isEmpty()) continue;

            for (SmsSkdRuleItem it : items) {
                Set<String> ds = parseDays(it.getDaysOfWeek());
                if (ds.isEmpty()) continue;

                // 上午
                if (ds.contains(dk + "_am")) {
                    SmsSkd s = new SmsSkd();
                    s.setDate(d.atTime(9, 0));
                    s.setStatus(1);
                    s.setRemain(it.getSkLimit());
                    s.setNoon(NoonCode.MORNING.getCode());
                    s.setStaffId(it.getStaffId());
                    s.setDeptId(rule.getDeptId());
                    s.setSkLimit(String.valueOf(it.getSkLimit()));
                    list.add(s);
                }
                // 下午
                if (ds.contains(dk + "_pm")) {
                    SmsSkd s = new SmsSkd();
                    s.setDate(d.atTime(15, 0));
                    s.setStatus(1);
                    s.setRemain(it.getSkLimit());
                    s.setNoon(NoonCode.AFTERNOON.getCode());
                    s.setStaffId(it.getStaffId());
                    s.setDeptId(rule.getDeptId());
                    s.setSkLimit(String.valueOf(it.getSkLimit()));
                    list.add(s);
                }
            }
        }

        if (!list.isEmpty()) {
            smsSkdMapper.insertBatch(list);
        }
    }
}

