package com.his.service;

import com.his.dto.RegistrationDto;
import com.his.vo.RegistrationPageVo;

import java.time.LocalDate;
import java.util.List;

public interface IDmsRegistrationService {

    /**
     * 患者挂号
     *
     * @param registrationDto 挂号信息
     */
    void registration(RegistrationDto registrationDto);

    /**
     * 分页查询挂号记录
     *
     * @param page 当前页（从 0 开始）
     * @param size 每页条数
     * @return 挂号记录列表
     */
    List<RegistrationPageVo> listByPage(int page, int size);

    /**
     * 挂号记录总数
     */
    long countAll();

    /**
     * 更新挂号的基础信息（科室、就诊日期等）
     *
     * @param id             挂号ID
     * @param deptId         科室ID
     * @param attendanceDate 就诊日期
     */
    void updateRegistrationBasic(Long id, Long deptId, Long doctorId, String session, LocalDate attendanceDate);

    /**
     * 删除挂号记录
     *
     * @param id 挂号ID
     */
    void deleteRegistration(Long id);

    /**
     * 退号：把挂号状态置为已取消（不删除记录）
     *
     * @param id 挂号ID
     */
    void cancelRegistration(Long id);
}
