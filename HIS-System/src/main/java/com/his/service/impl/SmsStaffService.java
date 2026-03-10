package com.his.service.impl;

import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsStaffMapper;
import com.his.service.ISmsStaffService;
import com.his.utils.IdGenerator;
import com.his.utils.JwtUtil;
import com.his.vo.SmsStaffLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsStaffService implements ISmsStaffService {

    @Autowired
    private SmsStaffMapper smsStallMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public SmsStaffLoginVo getInfo(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "token 无效");
        }

        SmsStaff smsStall = smsStallMapper.selectSmsStaffByUsername(username);
        if (smsStall == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        SmsStaffLoginVo smsStaffLoginVo = new SmsStaffLoginVo();
        smsStaffLoginVo.setId(smsStall.getId());
        smsStaffLoginVo.setUsername(smsStall.getUsername());
        smsStaffLoginVo.setName(smsStall.getName() == null ? "用户" : smsStall.getName());
        return smsStaffLoginVo;
    }

    @Override
    public void register(SmsStaffRegisterDTO smsStaffRegisterDTO) {
        if (smsStaffRegisterDTO == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "账号和密码不能为空");
        }

        if (smsStallMapper.selectSmsStaffCountByUsername(smsStaffRegisterDTO.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前注册账号已经存在");
        }

        SmsStaff smsStall = new SmsStaff();
        smsStall.setId(IdGenerator.generateNumericId());
        smsStall.setUsername(smsStaffRegisterDTO.getUsername());
        smsStall.setPassword(smsStaffRegisterDTO.getPassword());

        if (smsStallMapper.insertSmsStaff(smsStall) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "服务器错误，请联系管理员");
        }

    }

    @Override
    public SmsStaffLoginVo login(SmsStaffLoginDTO smsStaffLoginDTO) {
        if (smsStaffLoginDTO == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        // 检查账号密码是否正确
        SmsStaff smsStall = smsStallMapper.selectSmsStaffByUsername(smsStaffLoginDTO.getUsername());
        if (!smsStaffLoginDTO.getPassword().equals(smsStall.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "账号或密码错误");
        }

        // 生成 token
        String token = jwtUtil.generateToken(smsStall.getId(), smsStall.getUsername());

        SmsStaffLoginVo smsStaffLoginVo = new SmsStaffLoginVo();
        smsStaffLoginVo.setId(smsStall.getId());
        smsStaffLoginVo.setUsername(smsStall.getUsername());
        smsStaffLoginVo.setName(smsStall.getName() == null ? "用户" : smsStall.getName());
        smsStaffLoginVo.setToken(token);
        return smsStaffLoginVo;
    }

}
