package com.his.mapper;

import com.his.domain.BmsDailySettlement;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BmsDailySettlementMapper {

    int insert(BmsDailySettlement row);

    BmsDailySettlement selectById(@Param("id") Long id);

    List<BmsDailySettlement> selectByReportTimeRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    int updateAudit(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("auditorId") Long auditorId,
            @Param("auditorName") String auditorName,
            @Param("auditTime") LocalDateTime auditTime);
}
