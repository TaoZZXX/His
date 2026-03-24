package com.his.mapper;

import com.his.domain.SmsLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsLoginLogMapper {

    int insert(SmsLoginLog row);
}
