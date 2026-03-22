package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品模板主表（dms_drug_model）
 */
@Data
public class DmsDrugModel {

    private Long id;

    /** 状态（0 禁用 / 1 启用） */
    private Integer status;

    /** 模板名称 */
    private String name;

    /** 适用范围 */
    private Integer scope;

    /** 所属人 ID */
    private Long ownId;

    /** 适用病症 ID */
    private Long aim;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 模板编码 */
    private String code;

    /** 模板类型（0 西药 / 1 草药） */
    private Integer type;

    /** 副数 */
    private Long pairNum;

    /** 频次 */
    private Integer frequency;

    /** 治法 */
    private String therapy;

    /** 治法详情 */
    private String therapyDetails;

    /** 医嘱 */
    private String medicalAdvice;
}
