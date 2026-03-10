package com.his.service;

import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.vo.SmsStaffLoginVo;

public interface ISmsStaffService {

    public void register(SmsStaffRegisterDTO smsStaffRegisterDTO);

    public SmsStaffLoginVo login(SmsStaffLoginDTO userLoginDTO);

    public SmsStaffLoginVo getInfo(String token);
}
