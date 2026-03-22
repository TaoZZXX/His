package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DmsHerbalPrescriptionRecord {
    private Long id;

    /** 处方状态（0-未生效 1-已生效 2-已作废等） */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 处方总金额 */
    private BigDecimal amount;

    /** 处方名称/患者姓名 */
    private String name;

    /** 治法 */
    private String therapy;

    /** 治法详情 */
    private String therapyDetails;

    /** 医嘱 */
    private String medicalAdvice;

    /** 剂数 */
    private Long pairNum;

    /** 关联挂号ID */
    private Long registrationId;

    /** 服用频次（如1-每日1次 2-每日2次） */
    private Integer frequency;

    /** 服用方式（如1-水煎服 2-冲服等） */
    private Integer usageMeans;

    /** 处方类型（固定为中药类型） */
    private Integer type;

    /** 开方医护人员ID */
    private Long createStaffId;
}

