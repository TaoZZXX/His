package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface HomeDashboardMapper {

    long countPersonalRegistrationsOnDate(
            @Param("staffId") Long staffId,
            @Param("day") LocalDate day,
            @Param("canceledStatus") int canceledStatus
    );

    long countDeptRegistrationsOnDate(
            @Param("deptId") Long deptId,
            @Param("day") LocalDate day,
            @Param("canceledStatus") int canceledStatus
    );

    long countHospitalRegistrationsOnDate(
            @Param("day") LocalDate day,
            @Param("canceledStatus") int canceledStatus
    );

    List<Map<String, Object>> selectHospitalRegistrationCountByDay(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("canceledStatus") int canceledStatus
    );

    List<Map<String, Object>> selectFeeByItemTypeInPayTimeRange(
            @Param("start") String start,
            @Param("end") String end
    );

    List<Map<String, Object>> selectDeptItemTypeFeeInPayTimeRange(
            @Param("start") String start,
            @Param("end") String end
    );
}
