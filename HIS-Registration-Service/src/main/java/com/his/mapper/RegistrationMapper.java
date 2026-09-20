package com.his.mapper;

import com.his.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RegistrationMapper {
    List<RegistrationRankVo> selectRegistrationRanks();
    List<DeptVo> selectDepartments();
    List<DoctorVo> selectDoctorsByDept(@Param("deptId") Long deptId);
    List<DoctorVo> selectAvailableDoctors(@Param("deptId") Long deptId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("noon") Integer noon);
    List<Map<String, Object>> selectPatientByIdentificationNo(@Param("identificationNo") Long identificationNo);
    int insertPatient(Map<String, Object> row);
    Map<String, Object> selectPatientById(@Param("id") Long id);
    List<Long> selectSkdIds(@Param("doctorId") Long doctorId, @Param("deptId") Long deptId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("noon") Integer noon);
    int insertRegistration(Map<String, Object> row);
    BigDecimal selectRankPrice(@Param("rankId") Long rankId);
    int insertPayable(Map<String, Object> row);
    List<PayableItemVo> selectPayables(@Param("registrationId") Long registrationId);
    List<Long> selectUnpaidPayableIds(@Param("registrationId") Long registrationId);
    int markPayablePaid(@Param("id") Long id);
    Integer countUnpaidPayables(@Param("registrationId") Long registrationId);
    int updateRegistrationBindStatus(@Param("registrationId") Long registrationId, @Param("bindStatus") Integer bindStatus);
    int updateRegistrationBasic(@Param("id") Long id, @Param("deptId") Long deptId, @Param("skdId") Long skdId, @Param("attendanceDate") LocalDate attendanceDate);
    int deleteRegistration(@Param("id") Long id);
    int cancelRegistration(@Param("id") Long id, @Param("status") Integer status, @Param("endAttendance") Integer endAttendance);
    long countRegistrationPage(@Param("keyword") String keyword);
    List<RegistrationPageVo> selectRegistrationPage(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);
    int resetPayables(@Param("registrationId") Long registrationId);
}
