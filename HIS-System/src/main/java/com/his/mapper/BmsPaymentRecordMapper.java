package com.his.mapper;

import com.his.domain.BmsPaymentRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BmsPaymentRecordMapper {

    int insert(BmsPaymentRecord row);
}
