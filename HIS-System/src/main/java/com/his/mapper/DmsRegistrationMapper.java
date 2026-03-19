package com.his.mapper;

import com.his.domain.DmsRegistration;
import com.his.vo.OutpatientPatientVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DmsRegistrationMapper {

    Integer insertDmsRegistration(DmsRegistration dmsRegistration);

    /**
     * 分页查询挂号记录，按挂号时间倒序，返回 VO
     */
    List<com.his.vo.RegistrationPageVo> selectByPage(@Param("offset") Integer offset,
                                                     @Param("limit") Integer limit);

    /**
     * 统计总记录数
     */
    Long countAll();

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

}
