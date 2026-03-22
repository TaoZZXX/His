package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DmsMedicinePrescriptionRecord {
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

    /** 关联挂号ID */
    private Long registrationId;

    /** 退款状态（0-未退款 1-已退款等） */
    private Long refundStatus;

    /** 处方类型（1-西药 2-中药等） */
    private Integer type;

    /** 开方医护人员ID */
    private Long createStaffId;
}

