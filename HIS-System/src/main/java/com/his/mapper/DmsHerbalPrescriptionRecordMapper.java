package com.his.mapper;

import com.his.domain.DmsHerbalPrescriptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DmsHerbalPrescriptionRecordMapper {
    Integer insert(DmsHerbalPrescriptionRecord record);

    List<DmsHerbalPrescriptionRecord> selectByRegistrationId(@Param("registrationId") Long registrationId);

    BigDecimal sumAmountByRegistrationId(@Param("registrationId") Long registrationId);
}

