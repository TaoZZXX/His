package com.his.mapper;

import com.his.domain.DmsMedicineItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsMedicineItemRecordMapper {
    Integer insertBatch(@Param("list") List<DmsMedicineItemRecord> list);
    List<DmsMedicineItemRecord> selectByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
