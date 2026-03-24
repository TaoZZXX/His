package com.his.mapper;

import com.his.domain.DmsRegistration;
import com.his.vo.OutpatientPatientVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DmsRegistrationMapper {

    Integer insertDmsRegistration(DmsRegistration dmsRegistration);

    DmsRegistration selectById(@Param("id") Long id);

    /**
     * 分页查询挂号记录，按挂号时间倒序，返回 VO
     */
    List<com.his.vo.RegistrationPageVo> selectByPage(@Param("offset") Integer offset,
                                                     @Param("limit") Integer limit,
                                                     @Param("keyword") String keyword);

    /**
     * 统计总记录数（可选病历号/姓名模糊）
     */
    Long countAll(@Param("keyword") String keyword);

    /**
     * 就诊结束后标记已缴费
     */
    Integer markPaid(@Param("id") Long id);

    /**
     * 已缴费退回为未缴（退费）
     */
    Integer markUnpaid(@Param("id") Long id);

    /**
     * 直接设置收费标记（分项收费后按「是否仍有未缴明细」回写）
     */
    Integer updateBindStatus(@Param("id") Long id, @Param("bindStatus") Integer bindStatus);

    /**
     * 根据主键更新挂号的部分字段（科室、就诊日期等）
     */
    Integer updateById(DmsRegistration dmsRegistration);

    /**
     * 根据主键删除挂号记录
     */
    Integer deleteById(@Param("id") Long id);

    /**
     * 退号：修改状态为已取消（不物理删除）
     */
    Integer cancelById(DmsRegistration dmsRegistration);

    /**
     * 门诊医生工作台：查询指定医生/科室在某日期+午别的挂号病人列表（仅未结束就诊）
     * scope:
     * - self：本人（按 sms_skd.staff_id）
     * - dept：科室（按 dms_registration.dept_id）
     */
    List<OutpatientPatientVo> selectOutpatientDeskPatients(
            @Param("staffId") Long staffId,
            @Param("deptId") Long deptId,
            @Param("scope") String scope,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("noon") Integer noon,
            @Param("endAttendance") Integer endAttendance,
            @Param("canceledStatus") Integer canceledStatus,
            @Param("keyword") String keyword
    );

    /**
     * 门诊医生工作台：开始诊疗
     * - 将 dr.status 从 UNFINISHED -> FINISHED
     * - 但仍保持 end_attendance 为未完成（保证仍在“未结束就诊”范围）
     */
    Integer startVisitById(
            @Param("id") Long id,
            @Param("staffId") Long staffId,
            @Param("status") Integer status,
            @Param("unfinishedStatus") Integer unfinishedStatus,
            @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance,
            @Param("canceledStatus") Integer canceledStatus
    );

    /**
     * 门诊医生工作台：结束就诊
     * - 将 dr.end_attendance 从 UNFINISHED -> FINISHED
     * - 同时把 dr.status 设为 FINISHED
     */
    Integer finishVisitById(
            @Param("id") Long id,
            @Param("staffId") Long staffId,
            @Param("endAttendance") Integer endAttendance,
            @Param("status") Integer status,
            @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance,
            @Param("canceledStatus") Integer canceledStatus
    );

    /**
     * 科室视图：同科室任一医生可结束就诊（不校验排班医生是否为本人）
     */
    Integer finishVisitByDeptId(
            @Param("id") Long id,
            @Param("deptId") Long deptId,
            @Param("endAttendance") Integer endAttendance,
            @Param("status") Integer status,
            @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance,
            @Param("canceledStatus") Integer canceledStatus
    );

    /**
     * 科室视图：同科室任一医生可开始诊疗
     */
    Integer startVisitByDeptId(
            @Param("id") Long id,
            @Param("deptId") Long deptId,
            @Param("status") Integer status,
            @Param("unfinishedStatus") Integer unfinishedStatus,
            @Param("unfinishedEndAttendance") Integer unfinishedEndAttendance,
            @Param("canceledStatus") Integer canceledStatus
    );

    Integer countOperatePermission(@Param("registrationId") Long registrationId, @Param("staffId") Long staffId);

    Map<String, Object> selectPatientBaseByRegistrationId(@Param("registrationId") Long registrationId);

}
