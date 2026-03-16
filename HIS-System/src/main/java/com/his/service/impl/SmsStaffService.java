package com.his.service.impl;

import com.his.domain.PageResult;
import com.his.domain.SmsDept;
import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsDeptMapper;
import com.his.mapper.SmsStaffMapper;
import com.his.service.ISmsStaffService;
import com.his.utils.IdGenerator;
import com.his.utils.JwtUtil;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SmsStaffService implements ISmsStaffService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SmsStaffMapper smsStallMapper;

    @Autowired
    private SmsDeptMapper smsDeptMapper;

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
    public PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size, Integer deptId, Integer roleId) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        // convert to 0-based offset
        int offset = (page - 1) * size;

        Long total = smsStallMapper.selectStaffCount(deptId, roleId);
        List<StaffPageVo> list = smsStallMapper.selectStaffByPage(deptId, roleId, offset, size);

        for (StaffPageVo l : list) {
            Long deptIdT = l.getDeptId();
            if (deptIdT == null) {
                l.setDeptName("未知");
                continue;
            }
            SmsDept smsDept = smsDeptMapper.selectDeptById(deptIdT);
            if (smsDept == null) {
                l.setDeptName("未知");
                continue;
            }
            l.setDeptName(smsDept.getName());
        }

        // compute total pages
        int pages = 0;
        if (total != null && total > 0) {
            pages = (int) ((total + size - 1) / size);
        }

        PageResult<StaffPageVo> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total == null ? 0L : total);
        result.setPageNo(page);
        result.setPageSize(size);
        result.setPages(pages);
        return result;
    }

    // Backwards-compatible overload: support callers that pass only page and size
    public PageResult<StaffPageVo> getStaffByPage(Integer page, Integer size) {
        return getStaffByPage(page, size, null, null);
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
        smsStall.setCreateTime(LocalDateTime.now());

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

    @Override
    public void createStaff(SmsStaff smsStaff) {
        if (smsStaff == null || smsStaff.getUsername() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
        // check unique
        if (smsStallMapper.selectSmsStaffCountByUsername(smsStaff.getUsername()) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在");
        }
        smsStaff.setId(IdGenerator.generateNumericId());
        smsStaff.setCreateTime(LocalDateTime.now());
        // set create time to now if applicable (SmsStaff.createTime is LocalDate in model, keep null if not present)
        // insert
        if (smsStallMapper.insertSmsStaff(smsStaff) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "新增员工失败");
        }
    }

    @Override
    public void updateStaff(Long id, SmsStaff smsStaff) {
        if (id == null || smsStaff == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        SmsStaff exist = smsStallMapper.selectSmsStaffByUsername(smsStaff.getUsername() != null ? smsStaff.getUsername() : "__none__");
        if (exist != null && !exist.getId().equals(id)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已被占用");
        }
        smsStaff.setId(id);
        if (smsStallMapper.updateSmsStaff(smsStaff) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "更新员工失败");
        }
    }

    @Override
    public void deleteStaff(String username) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (smsStallMapper.deleteSmsStaffByUsername(username) < 1) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "删除员工失败");
        }
    }

}
