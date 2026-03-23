package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DmsDrugRefundItemRecord {
    private Long id;

    /** 退款状态（0-待处理 1-已完成 2-已拒绝） */
    private Integer status;

    /** 退款类型（1-药品退款 2-非药品退款） */
    private Integer type;

    /** 关联挂号ID */
    private Long registrationId;

    /** 关联药品ID */
    private Long drugId;

    /** 退款数量单位 */
    private Integer refundNumUnit;

    /** 退款数量 */
    private Long refundNum;

    /** 退款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    /** 退款执行人ID */
    private Long excutorId;

    /** 关联原处方项ID */
    private Long refundItemRecordId;
}

