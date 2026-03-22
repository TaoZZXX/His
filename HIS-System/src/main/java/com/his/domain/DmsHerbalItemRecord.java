package com.his.domain;

import lombok.Data;

@Data
public class DmsHerbalItemRecord {
    private Long id;

    /** 状态（0-正常 1-作废 2-已退款等） */
    private Integer status;

    /** 关联中药处方ID */
    private Long prescriptionId;

    /** 用药医嘱 */
    private String medicalAdvice;

    /** 脚注（如先煎、后下等） */
    private String footnote;

    /** 关联中药药品ID */
    private Long drugId;

    /** 单次用量 */
    private Long usageNum;

    /** 用量单位（如1-克 2-钱等） */
    private Integer usageNumUnit;

    /** 总数量 */
    private Long totalNum;

    /** 当前剩余数量 */
    private Long currentNum;
}

