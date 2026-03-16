package com.his.service;

import com.his.domain.PageResult;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.domain.SmsStaff;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;

public interface ISmsStaffService {

    void register(SmsStaffRegisterDTO smsStaffRegisterDTO);

    SmsStaffLoginVo login(SmsStaffLoginDTO userLoginDTO);

    SmsStaffLoginVo getInfo(String token);

    // 分页获取员工列表
    PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size, Integer deptId, Integer roleId);

    // create/update/delete staff
    void createStaff(SmsStaff smsStaff);

    void updateStaff(Long id, SmsStaff smsStaff);

    void deleteStaff(String username);

}
