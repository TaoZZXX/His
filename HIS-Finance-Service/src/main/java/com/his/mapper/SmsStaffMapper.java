package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsStaffMapper {
    String selectStaffDisplayNameById(@Param("id") Long id);
}
