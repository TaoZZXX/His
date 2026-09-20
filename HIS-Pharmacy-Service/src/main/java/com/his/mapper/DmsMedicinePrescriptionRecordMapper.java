package com.his.mapper;

import com.his.domain.DmsMedicinePrescriptionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DmsMedicinePrescriptionRecordMapper {
    DmsMedicinePrescriptionRecord selectById(@Param("id") Long id);
}
