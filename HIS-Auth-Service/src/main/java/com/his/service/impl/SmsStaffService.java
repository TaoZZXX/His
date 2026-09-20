package com.his.service.impl;

import com.his.domain.PageResult;
import com.his.domain.SmsDept;
import com.his.domain.SmsLoginLog;
import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.dto.LoginLogEvent;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsDeptMapper;
import com.his.mapper.SmsLoginLogMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.service.ISmsStaffService;
import com.his.service.LoginLogEventPublisher;
import com.his.utils.JwtUtil;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SmsStaffService implements ISmsStaffService {

    private final JwtUtil jwtUtil;
    private final SmsStaffMapper smsStaffMapper;
    private final SmsDeptMapper smsDeptMapper;
    private final SmsLoginLogMapper smsLoginLogMapper;
    private final LoginLogEventPublisher loginLogEventPublisher;

    public SmsStaffService(JwtUtil jwtUtil,
                           SmsStaffMapper smsStaffMapper,
                           SmsDeptMapper smsDeptMapper,
                           SmsLoginLogMapper smsLoginLogMapper,
                           LoginLogEventPublisher loginLogEventPublisher) {
        this.jwtUtil = jwtUtil;
        this.smsStaffMapper = smsStaffMapper;
        this.smsDeptMapper = smsDeptMapper;
        this.smsLoginLogMapper = smsLoginLogMapper;
        this.loginLogEventPublisher = loginLogEventPublisher;
    }

    @Override
    public void register(SmsStaffRegisterDTO dto) {
        if (dto == null || isBlank(dto.getUsername()) || isBlank(dto.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "账号和密码不能为空");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次密码不一致");
        }
        if (smsStaffMapper.selectSmsStaffCountByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前注册账号已经存在");
        }
        SmsStaff smsStaff = new SmsStaff();
        smsStaff.setUsername(dto.getUsername().trim());
        smsStaff.setPassword(dto.getPassword());
        smsStaff.setCreateTime(LocalDateTime.now());
        smsStaff.setStatus(1);
        if (smsStaffMapper.insertSmsStaff(smsStaff) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "服务器错误，请联系管理员");
        }
    }

    @Override
    public SmsStaffLoginVo login(SmsStaffLoginDTO dto) {
        if (dto == null || isBlank(dto.getUsername()) || isBlank(dto.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        SmsStaff smsStaff = smsStaffMapper.selectSmsStaffByUsername(dto.getUsername().trim());
        if (smsStaff == null || smsStaff.getPassword() == null || !smsStaff.getPassword().equals(dto.getPassword())) {
            writeLoginLog(null);
            throw new BusinessException(ResultCode.LOGIN_FAILED, "账号或密码错误");
        }
        String token = jwtUtil.generateToken(smsStaff.getId(), smsStaff.getUsername());
        writeLoginLog(smsStaff.getId());
        SmsStaffLoginVo vo = new SmsStaffLoginVo();
        vo.setId(smsStaff.getId());
        vo.setUsername(smsStaff.getUsername());
        vo.setName(smsStaff.getName() == null ? "用户" : smsStaff.getName());
        vo.setToken(token);
        return vo;
    }

    @Override
    public SmsStaffLoginVo getInfo(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "token 无效");
        }
        SmsStaff smsStaff = smsStaffMapper.selectSmsStaffByUsername(username);
        if (smsStaff == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        SmsStaffLoginVo vo = new SmsStaffLoginVo();
        vo.setId(smsStaff.getId());
        vo.setUsername(smsStaff.getUsername());
        vo.setName(smsStaff.getName() == null ? "用户" : smsStaff.getName());
        return vo;
    }

    @Override
    public PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size, Integer deptId, Integer roleId) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int offset = (page - 1) * size;
        Long total = smsStaffMapper.selectStaffCount(deptId, roleId);
        List<StaffPageVo> list = smsStaffMapper.selectStaffByPage(deptId, roleId, offset, size);
        for (StaffPageVo l : list) {
            if (l.getDeptId() == null) {
                l.setDeptName("未知");
                continue;
            }
            SmsDept dept = smsDeptMapper.selectDeptById(l.getDeptId());
            l.setDeptName(dept == null ? "未知" : dept.getName());
        }
        PageResult<StaffPageVo> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total == null ? 0L : total);
        result.setPageNo(page);
        result.setPageSize(size);
        result.setPages((int) ((result.getTotal() + size - 1) / size));
        return result;
    }

    @Override
    public void createStaff(SmsStaff smsStaff) {
        if (smsStaff == null || isBlank(smsStaff.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
        if (smsStaffMapper.selectSmsStaffCountByUsername(smsStaff.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在");
        }
        smsStaff.setCreateTime(LocalDateTime.now());
        if (smsStaffMapper.insertSmsStaff(smsStaff) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "新增员工失败");
        }
    }

    @Override
    public void updateStaff(Long id, SmsStaff smsStaff) {
        if (id == null || smsStaff == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (!isBlank(smsStaff.getUsername())) {
            SmsStaff exist = smsStaffMapper.selectSmsStaffByUsername(smsStaff.getUsername());
            if (exist != null && !exist.getId().equals(id)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已被占用");
            }
        }
        smsStaff.setId(id);
        if (smsStaffMapper.updateSmsStaff(smsStaff) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "更新员工失败");
        }
    }

    @Override
    public void deleteStaff(String username) {
        if (isBlank(username)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (smsStaffMapper.deleteSmsStaffByUsername(username) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "删除员工失败");
        }
    }

    private void writeLoginLog(Long userId) {
        try {
            LoginLogEvent event = new LoginLogEvent();
            event.setUserId(userId);
            event.setCreateTime(LocalDateTime.now());
            event.setIp(resolveClientIp());
            loginLogEventPublisher.publish(event);
        } catch (Exception ignored) {
            // MQ 异常时兜底直写库，避免日志彻底丢失
            try {
                SmsLoginLog log = new SmsLoginLog();
                log.setUserId(userId);
                log.setCreateTime(LocalDateTime.now());
                log.setIp(resolveClientIp());
                smsLoginLogMapper.insert(log);
            } catch (Exception ignoredAgain) {
            }
        }
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null || attrs.getRequest() == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (isBlank(ip)) ip = request.getHeader("X-Real-IP");
        if (isBlank(ip)) ip = request.getRemoteAddr();
        return isBlank(ip) ? "unknown" : ip;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
