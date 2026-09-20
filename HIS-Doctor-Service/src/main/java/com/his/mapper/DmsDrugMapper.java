package com.his.mapper;

import com.his.domain.DmsDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsDrugMapper {
    DmsDrug selectById(@Param("id") Long id);
    List<DmsDrug> selectEnabledByKeyword(@Param("keyword") String keyword);
}
