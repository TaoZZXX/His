package com.his.service;

import com.his.domain.PageResult;
import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;

public interface ISmsStaffService {
    void register(SmsStaffRegisterDTO smsStaffRegisterDTO);
    SmsStaffLoginVo login(SmsStaffLoginDTO userLoginDTO);
    SmsStaffLoginVo getInfo(String token);
    PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size, Integer deptId, Integer roleId);
    void createStaff(SmsStaff smsStaff);
    void updateStaff(Long id, SmsStaff smsStaff);
    void deleteStaff(String username);
}
