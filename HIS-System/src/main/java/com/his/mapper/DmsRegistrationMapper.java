package com.his.mapper;

import com.his.domain.DmsRegistration;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DmsRegistrationMapper {

    public Integer insertDmsRegistration(DmsRegistration dmsRegistration);

}
