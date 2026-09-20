package com.his.mapper;

import com.his.domain.DmsNonDrugItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DmsNonDrugItemRecordMapper {
    Integer insert(DmsNonDrugItemRecord record);
    List<DmsNonDrugItemRecord> selectByRegistrationIdAndType(@Param("registrationId") Long registrationId, @Param("type") Integer type);
    BigDecimal sumAmountByRegistrationId(@Param("registrationId") Long registrationId);
}
