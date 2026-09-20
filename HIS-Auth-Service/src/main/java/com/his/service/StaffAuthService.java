package com.his.service;

import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.mapper.SmsStaffMapper;
import com.his.utils.JwtUtil;
import com.his.vo.SmsStaffLoginVo;
import org.springframework.stereotype.Service;

@Service
public class StaffAuthService {

    private final SmsStaffMapper smsStaffMapper;
    private final JwtUtil jwtUtil;

    public StaffAuthService(SmsStaffMapper smsStaffMapper, JwtUtil jwtUtil) {
        this.smsStaffMapper = smsStaffMapper;
        this.jwtUtil = jwtUtil;
    }

    public SmsStaffLoginVo login(SmsStaffLoginDTO dto) {
        if (dto == null || isBlank(dto.getUsername()) || isBlank(dto.getPassword())) {
            throw new IllegalArgumentException("参数错误");
        }
        SmsStaff row = smsStaffMapper.selectSmsStaffByUsername(dto.getUsername().trim());
        if (row == null || row.getPassword() == null || !row.getPassword().equals(dto.getPassword())) {
            throw new SecurityException("LOGIN_FAILED");
        }
        SmsStaffLoginVo vo = new SmsStaffLoginVo();
        vo.setId(row.getId());
        vo.setUsername(row.getUsername());
        vo.setName(isBlank(row.getName()) ? "用户" : row.getName());
        vo.setToken(jwtUtil.generateToken(row.getId(), row.getUsername()));
        return vo;
    }

    public SmsStaffLoginVo info(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        if (isBlank(username)) {
            throw new SecurityException("TOKEN_INVALID");
        }
        SmsStaff row = smsStaffMapper.selectSmsStaffByUsername(username);
        if (row == null) {
            throw new SecurityException("USER_NOT_EXIST");
        }
        SmsStaffLoginVo vo = new SmsStaffLoginVo();
        vo.setId(row.getId());
        vo.setUsername(row.getUsername());
        vo.setName(isBlank(row.getName()) ? "用户" : row.getName());
        return vo;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
