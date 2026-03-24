package com.his.mapper;

import com.his.domain.BmsPaymentAllocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BmsPaymentAllocationMapper {

    int insert(BmsPaymentAllocation row);

    int insertBatch(@org.apache.ibatis.annotations.Param("list") List<BmsPaymentAllocation> list);

    int deleteByPaymentId(@org.apache.ibatis.annotations.Param("paymentId") Long paymentId);

    List<Map<String, Object>> selectAllocationRowsInPayRange(
            @org.apache.ibatis.annotations.Param("rangeStart") java.time.LocalDateTime rangeStart,
            @org.apache.ibatis.annotations.Param("rangeEnd") java.time.LocalDateTime rangeEnd
    );
}
