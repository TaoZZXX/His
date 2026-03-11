package com.his.service.impl;

import com.his.domain.PmsPatient;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.PmsPatientMapper;
import com.his.service.IPmsPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PmsPatientServiceService implements IPmsPatientService {

    @Autowired
    private PmsPatientMapper pmsPatientMapper;


    @Override
    public Boolean isPatientExist(Long identificationNo) {
        Integer i = pmsPatientMapper.countByIdentificationNo(identificationNo);
        return i != null && i > 0;
    }

    @Override
    public void insertPmsPatient(PmsPatient pmsPatient) {
        Integer i = pmsPatientMapper.insertPmsPatient(pmsPatient);
        if (i == null || i <= 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "插入患者记录失败");
        }
    }

    @Override
    public PmsPatient selectPmsPatientByIdentificationNo(Long identificationNo) {
        return pmsPatientMapper.selectPmsPatientByIdentificationNo(identificationNo);
    }
}
