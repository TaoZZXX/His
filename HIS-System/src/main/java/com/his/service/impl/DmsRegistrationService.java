package com.his.service.impl;

import com.his.domain.DmsRegistration;
import com.his.domain.PmsPatient;
import com.his.dto.RegistrationDto;
import com.his.enums.EndAttendanceCode;
import com.his.enums.GenderCode;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.enums.NoonCode;
import com.his.exception.BusinessException;
import com.his.domain.SmsRegistrationRank;
import com.his.mapper.DmsRegistrationMapper;
import com.his.mapper.SmsRegistrationRankMapper;
import com.his.mapper.SmsSkdMapper;
import com.his.service.IBmsCashierService;
import com.his.service.IDmsRegistrationService;
import com.his.service.IPmsPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DmsRegistrationService implements IDmsRegistrationService {

    @Autowired
    private DmsRegistrationMapper dmsRegistrationMapper;

    @Autowired
    private IPmsPatientService iPmsPatientService;

    @Autowired
    private SmsSkdMapper smsSkdMapper;

    @Autowired
    private IBmsCashierService bmsCashierService;

    @Autowired
    private SmsRegistrationRankMapper smsRegistrationRankMapper;

    private Integer parseNoon(String session) {
        if (session == null) return null;
        if ("上午".equals(session)) return NoonCode.MORNING.getCode();
        if ("下午".equals(session)) return NoonCode.AFTERNOON.getCode();
        return null;
    }

    private Long resolveSkdId(Long doctorId, Long deptId, LocalDate attendanceDate, String session) {
        if (doctorId == null || deptId == null || attendanceDate == null) return null;
        LocalDateTime start = attendanceDate.atStartOfDay();
        LocalDateTime end = attendanceDate.plusDays(1).atStartOfDay();
        Integer noon = parseNoon(session);
        List<com.his.domain.SmsSkd> list = smsSkdMapper.selectByStaffDeptAndDateRange(doctorId, deptId, start, end, noon);
        if (list == null || list.isEmpty()) return null;
        return list.get(0).getId();
    }

    private BigDecimal resolveRegistrationFee(RegistrationDto dto) {
        if (dto.getRankId() != null) {
            SmsRegistrationRank rank = smsRegistrationRankMapper.selectById(dto.getRankId());
            if (rank == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的挂号号别");
            }
            if (rank.getStatus() != null && rank.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "该号别已停用");
            }
            BigDecimal p = rank.getPrice() == null ? BigDecimal.ZERO : rank.getPrice();
            return p.setScale(2, RoundingMode.HALF_UP);
        }
        if (dto.getAmount() != null) {
            return BigDecimal.valueOf(dto.getAmount()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public Long registration(RegistrationDto registrationDto) {
        if (registrationDto == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号信息不能为空");
        }

        try {
            /// 检查患者是否存在
            /// 如果不存在，则创建新的患者记录
            PmsPatient pmsPatient = iPmsPatientService.selectPmsPatientByIdentificationNo(registrationDto.getIdentificationNo());
            if (pmsPatient == null) {
                pmsPatient = new PmsPatient();
                pmsPatient.setName(registrationDto.getName());
                pmsPatient.setIdentificationNo(registrationDto.getIdentificationNo());
                pmsPatient.setDateOfBirth(registrationDto.getBirthDate());
                pmsPatient.setGender(GenderCode.MALE.getDescription().equals(registrationDto.getGender()) ? GenderCode.MALE.getCode() : GenderCode.FEMALE.getCode());
                pmsPatient.setHomeAddress(registrationDto.getAddress());
                pmsPatient.setMedicalRecordNo(registrationDto.getMedicalRecord());
                pmsPatient.setPhoneNo(registrationDto.getContact());
                iPmsPatientService.insertPmsPatient(pmsPatient);
            }

            DmsRegistration dmsRegistration = new DmsRegistration();
            dmsRegistration.setPatientId(pmsPatient.getId());
            dmsRegistration.setCreateTime(LocalDate.now()); /// 设置挂号时间为当前日期
            dmsRegistration.setEndAttendance(EndAttendanceCode.UNFINISHED.getCode()); /// 未结束
            /// 与医生工作台候诊队列一致：新建为「未完成」，医生「开始诊疗」后再置为 FINISHED
            dmsRegistration.setStatus(RegistrationStatusCode.UNFINISHED.getCode());
            dmsRegistration.setDeptId(registrationDto.getDepartmentId());
            dmsRegistration.setAttendanceDate(registrationDto.getRegistrationDate());
            Long skdId = resolveSkdId(registrationDto.getDoctorId(), registrationDto.getDepartmentId(),
                    registrationDto.getRegistrationDate(), registrationDto.getSession());
            if (skdId == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "未找到匹配的医生排班");
            }
            dmsRegistration.setSkdId(skdId);
            dmsRegistration.setNeedBook("是".equals(registrationDto.getMedicalRecord()) ? 1 : 0);
            dmsRegistration.setBindStatus(0);
            dmsRegistrationMapper.insertDmsRegistration(dmsRegistration);
            Long regId = dmsRegistration.getId();
            BigDecimal regFee = resolveRegistrationFee(registrationDto);
            bmsCashierService.createRegistrationFeePayable(regId, regFee);
            return regId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ResultCode.SERVER_ERROR, "挂号失败");
        }
    }

    @Override
    public List<com.his.vo.RegistrationPageVo> listByPage(int page, int size, String keyword) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        int offset = page * size;
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return dmsRegistrationMapper.selectByPage(offset, size, kw);
    }

    @Override
    public long countAll(String keyword) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        Long total = dmsRegistrationMapper.countAll(kw);
        return total == null ? 0L : total;
    }

    @Override
    public Long markRegistrationPaid(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        return bmsCashierService.payAllUnpaid(id, null, 1);
    }

    @Override
    public void markRegistrationRefund(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        bmsCashierService.resetRegistrationPayments(id);
    }

    @Override
    public void updateRegistrationBasic(Long id, Long deptId, Long doctorId, String session, LocalDate attendanceDate) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        DmsRegistration reg = new DmsRegistration();
        reg.setId(id);
        reg.setDeptId(deptId);
        reg.setAttendanceDate(attendanceDate);
        Long skdId = resolveSkdId(doctorId, deptId, attendanceDate, session);
        if (skdId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "未找到匹配的医生排班");
        }
        reg.setSkdId(skdId);
        int rows = dmsRegistrationMapper.updateById(reg);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "更新挂号信息失败");
        }
    }

    @Override
    public void deleteRegistration(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        Integer rows = dmsRegistrationMapper.deleteById(id);
        if (rows == null || rows <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "删除挂号记录失败");
        }
    }

    @Override
    public void cancelRegistration(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号ID不能为空");
        }
        DmsRegistration reg = new DmsRegistration();
        reg.setId(id);
        reg.setStatus(RegistrationStatusCode.CANCELED.getCode());
        reg.setEndAttendance(EndAttendanceCode.FINISHED.getCode());
        Integer rows = dmsRegistrationMapper.cancelById(reg);
        if (rows == null || rows <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "退号失败");
        }
    }
}
