package com.his.mapper;

import com.his.domain.DmsCaseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DmsCaseHistoryMapper {
    DmsCaseHistory selectByRegistrationId(@Param("registrationId") Long registrationId);

    Integer insert(DmsCaseHistory record);

    Integer updateByRegistrationId(DmsCaseHistory record);
}

