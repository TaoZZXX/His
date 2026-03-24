package com.his.mapper;

import com.his.domain.BmsPaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsPaymentRecordMapper {

    int insert(BmsPaymentRecord row);

    List<Long> selectIdsByRegistrationId(@Param("registrationId") Long registrationId);

    int deleteByRegistrationId(@Param("registrationId") Long registrationId);

    List<Map<String, Object>> selectPayMethodSummaryInRange(
            @Param("rangeStart") java.time.LocalDateTime rangeStart,
            @Param("rangeEnd") java.time.LocalDateTime rangeEnd
    );
}
