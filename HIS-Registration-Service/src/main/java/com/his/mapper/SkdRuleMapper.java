package com.his.mapper;

import com.his.vo.SkdRuleItemVo;
import com.his.vo.SkdRuleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SkdRuleMapper {
    int insertRule(Map<String, Object> row);
    int insertRuleItem(Map<String, Object> row);
    List<SkdRuleVo> selectRules(@Param("deptId") Long deptId);
    List<SkdRuleVo> selectRuleById(@Param("id") Long id);
    List<SkdRuleItemVo> selectRuleItems(@Param("ruleId") Long ruleId);
    int publishRule(@Param("id") Long id);
    int insertSkd(@Param("date") LocalDateTime date, @Param("status") int status, @Param("remain") Long remain, @Param("noon") int noon,
                  @Param("staffId") Long staffId, @Param("deptId") Long deptId, @Param("skLimit") String skLimit);
}
