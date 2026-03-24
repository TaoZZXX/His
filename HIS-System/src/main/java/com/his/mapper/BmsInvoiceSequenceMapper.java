package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BmsInvoiceSequenceMapper {

    int allocateNext(@Param("seqName") String seqName);

    Long selectLastInsertId();
}
