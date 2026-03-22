package com.his.mapper;

import com.his.domain.SmsSkdRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsSkdRuleMapper {

    Integer insert(SmsSkdRule rule);

    Integer updateStatus(@Param("id") Long id,
                         @Param("status") Integer status);

    SmsSkdRule selectById(@Param("id") Long id);

    List<SmsSkdRule> selectByDeptId(@Param("deptId") Long deptId);
}

