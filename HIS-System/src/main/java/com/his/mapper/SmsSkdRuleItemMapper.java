package com.his.mapper;

import com.his.domain.SmsSkdRuleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsSkdRuleItemMapper {

    Integer insertBatch(@Param("items") List<SmsSkdRuleItem> items);

    List<SmsSkdRuleItem> selectByRuleId(@Param("skRuleId") Long skRuleId);

    Integer deleteByRuleId(@Param("skRuleId") Long skRuleId);
}

