package com.his.mapper;

import com.his.domain.DmsMedicineItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsMedicineItemRecordMapper {
    List<DmsMedicineItemRecord> selectByIds(@Param("ids") List<Long> ids);
    int dispenseByIds(@Param("ids") List<Long> ids);
    int refundLineByIds(@Param("ids") List<Long> ids);
}
