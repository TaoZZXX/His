package com.his.mapper;

import com.his.domain.PmsPatient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PmsPatientMapper {

    /**
     * 根据身份证号码/证件号码统计患者数量
     * @param identificationNo 身份证号码/证件号码
     * @return 患者数量
     */
    public Integer countByIdentificationNo(Long identificationNo);

    /**
     * 插入新的患者记录
     * @param pmsPatient 患者对象，包含患者的基本信息
     * @return 插入操作影响的行数，通常为 1 如果插入成功
     */
    public Integer insertPmsPatient(PmsPatient pmsPatient);

    /**
     * 根据身份证号码/证件号码查询患者信息
     * @param identificationNo 身份证号码/证件号码
     * @return 患者对象，如果找到匹配的患者；如果没有找到，则返回 null
     */
    public PmsPatient selectPmsPatientByIdentificationNo(Long identificationNo);

}
