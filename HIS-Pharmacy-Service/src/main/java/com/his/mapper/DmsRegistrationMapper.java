package com.his.mapper;

import com.his.domain.DmsRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DmsRegistrationMapper {
    DmsRegistration selectById(@Param("id") Long id);
}
