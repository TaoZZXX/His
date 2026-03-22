package com.his.mapper;

import com.his.domain.DmsMedicineItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsMedicineItemRecordMapper {
    Integer insertBatch(@Param("list") List<DmsMedicineItemRecord> list);

    List<DmsMedicineItemRecord> selectByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    DmsMedicineItemRecord selectById(@Param("id") Long id);

    List<DmsMedicineItemRecord> selectByIds(@Param("ids") List<Long> ids);

    /** 发药：待发数量清零 */
    int dispenseByIds(@Param("ids") List<Long> ids);

    /** 退药：整行退（演示版，不联动收银退费） */
    int refundLineByIds(@Param("ids") List<Long> ids);
}

