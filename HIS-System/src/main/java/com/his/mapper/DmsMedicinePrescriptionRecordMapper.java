package com.his.mapper;

import com.his.domain.DmsMedicinePrescriptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DmsMedicinePrescriptionRecordMapper {
    Integer insert(DmsMedicinePrescriptionRecord record);

    DmsMedicinePrescriptionRecord selectById(@Param("id") Long id);

    List<DmsMedicinePrescriptionRecord> selectByRegistrationId(@Param("registrationId") Long registrationId);

    BigDecimal sumAmountByRegistrationId(@Param("registrationId") Long registrationId);
}

