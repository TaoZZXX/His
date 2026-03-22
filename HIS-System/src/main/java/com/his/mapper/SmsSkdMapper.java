package com.his.mapper;

import com.his.domain.SmsSkd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SmsSkdMapper {

    /**
     * 根据科室、医生、日期范围、午别查询排班
     */
    List<SmsSkd> selectByStaffDeptAndDateRange(@Param("staffId") Long staffId,
                                               @Param("deptId") Long deptId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("noon") Integer noon);

    /**
     * 查询某天某科室可挂号医生ID列表（按排班）
     */
    List<Long> selectAvailableStaffIds(@Param("deptId") Long deptId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("noon") Integer noon);

    /**
     * 批量插入排班
     */
    Integer insertBatch(@Param("list") List<SmsSkd> list);
}

