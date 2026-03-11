package com.his.service.impl;

import com.his.domain.DmsRegistration;
import com.his.domain.PmsPatient;
import com.his.dto.RegistrationDto;
import com.his.enums.EndAttendanceCode;
import com.his.enums.GenderCode;
import com.his.enums.RegistrationStatusCode;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.DmsRegistrationMapper;
import com.his.mapper.PmsPatientMapper;
import com.his.service.IDmsRegistrationService;
import com.his.service.IPmsPatientService;
import com.his.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DmsRegistrationService implements IDmsRegistrationService {

    @Autowired
    private DmsRegistrationMapper dmsRegistrationMapper;

    @Autowired
    private IPmsPatientService iPmsPatientService;


    @Override
    @Transactional
    public void registration(RegistrationDto registrationDto) {
        if (registrationDto == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "挂号信息不能为空");
        }

        try {
            /// 检查患者是否存在
            /// 如果不存在，则创建新的患者记录
            PmsPatient pmsPatient = iPmsPatientService.selectPmsPatientByIdentificationNo(registrationDto.getIdentificationNo());
            if (pmsPatient == null) {
                pmsPatient = new PmsPatient();
                pmsPatient.setId(IdGenerator.generateNumericId());
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
            dmsRegistration.setId(IdGenerator.generateNumericId()); /// 生成新的挂号 ID
            dmsRegistration.setPatientId(pmsPatient.getId());
            dmsRegistration.setCreateTime(LocalDate.now()); /// 设置挂号时间为当前日期
            dmsRegistration.setEndAttendance(EndAttendanceCode.UNFINISHED.getCode()); /// 未结束
            dmsRegistration.setStatus(RegistrationStatusCode.FINISHED.getCode());   /// 挂号完成
            dmsRegistration.setSkdId(null); // TODO
            dmsRegistration.setNeedBook("是".equals(registrationDto.getMedicalRecord()) ? 1 : 0);
            dmsRegistration.setBindStatus(null); // TODO
            // TODO 还有些字段需要设置
            dmsRegistrationMapper.insertDmsRegistration(dmsRegistration);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ResultCode.SERVER_ERROR, "挂号失败");
        }
    }
}
