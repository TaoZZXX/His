package com.his.mapper;

import com.his.vo.OutpatientPatientVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DmsRegistrationMapper {
    List<OutpatientPatientVo> selectOutpatientDeskPatients(@Param("staffId") Long staffId, @Param("deptId") Long deptId, @Param("scope") String scope, @Param("attendanceDate") LocalDate attendanceDate,
                                                           @Param("noon") Integer noon, @Param("endAttendance") Integer endAttendance, @Param("canceledStatus") Integer canceledStatus, @Param("keyword") String keyword);
    Integer startVisitById(@Param("id") Long id, @Param("staffId") Long staffId, @Param("status") Integer status, @Param("unfinishedStatus") Integer unfinishedStatus, @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance, @Param("canceledStatus") Integer canceledStatus);
    Integer startVisitByDeptId(@Param("id") Long id, @Param("deptId") Long deptId, @Param("status") Integer status, @Param("unfinishedStatus") Integer unfinishedStatus, @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance, @Param("canceledStatus") Integer canceledStatus);
    Integer finishVisitById(@Param("id") Long id, @Param("staffId") Long staffId, @Param("endAttendance") Integer endAttendance, @Param("status") Integer status, @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance, @Param("canceledStatus") Integer canceledStatus);
    Integer finishVisitByDeptId(@Param("id") Long id, @Param("deptId") Long deptId, @Param("endAttendance") Integer endAttendance, @Param("status") Integer status, @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance, @Param("canceledStatus") Integer canceledStatus);
    Integer countOperatePermission(@Param("registrationId") Long registrationId, @Param("staffId") Long staffId);
    Map<String, Object> selectPatientBaseByRegistrationId(@Param("registrationId") Long registrationId);
}
