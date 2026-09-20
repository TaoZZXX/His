package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsStaffMapper {
    Integer selectDeptIdById(@Param("id") Long id);
}
