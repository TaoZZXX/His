package com.his.mapper;

import com.his.domain.SmsDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsDeptMapper {
    SmsDept selectDeptById(@Param("id") Long id);
}
