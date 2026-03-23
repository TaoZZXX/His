package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DmsNonDrugItemRecord {
    private Long id;

    /** 关联挂号ID */
    private Long registrationId;

    /** 状态（0-未执行 1-已执行 2-已作废等） */
    private Integer status;

    /** 检查/治疗目的 */
    private String aim;

    /** 需求描述 */
    private String demand;

    /** 记录状态（如0-待审核 1-已审核） */
    private Integer logStatus;

    /** 检查结果文本 */
    private String checkResult;

    /** 结果图片URL列表（逗号分隔） */
    private String resultImgUrlList;

    /** 临床印象 */
    private String clinicalImpression;

    /** 临床诊断 */
    private String clinicalDiagnosis;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 执行人员ID */
    private Long excuteStaffId;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime logDatetime;

    /** 关联非药品项目ID */
    private Long noDrugId;

    /** 检查部位 */
    private String checkParts;

    /** 项目类型（1-检查 2-治疗 3-检验等） */
    private Integer type;

    /** 执行科室ID */
    private Long excuteDeptId;

    /** 开单人员ID */
    private Long createStaffId;

    /** 记录人员ID */
    private Long logStaffId;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime excuteTime;

    /** 项目金额 */
    private BigDecimal amount;
}

