package com.his.service;

import com.his.domain.PmsPatient;

public interface IPmsPatientService {

    /**
     * 检查患者是否存在
     * @param identificationNo 身份证号码/证件号码
     * @return true 如果患者存在，false 如果患者不存在
     */
    public Boolean isPatientExist(Long identificationNo);

    /**
     * 插入新的患者记录
     * @param pmsPatient 患者对象，包含患者的基本信息
     */
    public void insertPmsPatient(PmsPatient pmsPatient);

    /**
     * 根据身份证号码/证件号码查询患者信息
     * @param identificationNo 身份证号码/证件号码
     * @return 患者对象，如果找到匹配的患者；如果没有找到，则返回 null
     */
    public PmsPatient selectPmsPatientByIdentificationNo(Long identificationNo);

}
