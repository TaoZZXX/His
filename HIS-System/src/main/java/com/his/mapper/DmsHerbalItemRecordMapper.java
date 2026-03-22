package com.his.mapper;

import com.his.domain.DmsHerbalItemRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsHerbalItemRecordMapper {
    Integer insertBatch(@Param("list") List<DmsHerbalItemRecord> list);

    List<DmsHerbalItemRecord> selectByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}

