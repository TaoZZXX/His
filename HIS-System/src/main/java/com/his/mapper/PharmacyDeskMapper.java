package com.his.mapper;

import com.his.vo.PharmacyMedicineLineVo;
import com.his.vo.PharmacyPatientQueueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PharmacyDeskMapper {

    List<PharmacyPatientQueueVo> selectPatientQueue(@Param("mode") String mode,
                                                     @Param("keyword") String keyword,
                                                     @Param("limit") int limit);

    List<PharmacyMedicineLineVo> selectMedicineLines(@Param("registrationId") Long registrationId);
}
