package com.his.mapper;

import com.his.domain.BmsPaymentAllocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BmsPaymentAllocationMapper {

    int insert(BmsPaymentAllocation row);

    int insertBatch(@org.apache.ibatis.annotations.Param("list") List<BmsPaymentAllocation> list);
}
