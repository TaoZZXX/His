package com.his.service;

import com.his.domain.Result;
import com.his.dto.DateRangeDto;
import com.his.dto.SkdRuleCreateDto;
import com.his.vo.IdResponseVo;
import com.his.vo.SkdRuleDetailVo;
import com.his.vo.SkdRuleVo;

import java.util.List;
import java.util.Map;

public interface ISkdRuleService {
    Result<IdResponseVo> createRule(SkdRuleCreateDto dto);
    Result<List<SkdRuleVo>> listRules(Long deptId);
    Result<SkdRuleDetailVo> getRuleDetail(Long id);
    Result<Object> publish(Long id);
    Result<Object> generate(Long id, DateRangeDto body);
}
