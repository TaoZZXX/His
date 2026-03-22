package com.his.mapper;

import com.his.domain.BmsInvoiceRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BmsInvoiceRecordMapper {

    int insert(BmsInvoiceRecord row);
}
