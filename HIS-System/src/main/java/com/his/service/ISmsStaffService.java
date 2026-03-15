package com.his.service;

import com.his.domain.PageResult;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;

public interface ISmsStaffService {

    public void register(SmsStaffRegisterDTO smsStaffRegisterDTO);

    public SmsStaffLoginVo login(SmsStaffLoginDTO userLoginDTO);

    public SmsStaffLoginVo getInfo(String token);

    // 分页获取员工列表
    public PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size, Integer deptId, Integer roleId);

}
