package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工工作量记录表（sms_workload_record）
 */
@Data
public class SmsWorkloadRecord {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private Long staffId;

    /** 药品数量 */
    private BigDecimal medicineAmount;

    /** 草药数量 */
    private BigDecimal herbalAmount;

    /** 检查数量 */
    private BigDecimal checkAmount;

    /** 处置数量 */
    private BigDecimal dispositionAmount;

    /** 挂号数量 */
    private BigDecimal registrationAmount;

    /** 检验数量 */
    private BigDecimal testAmount;

    /** 总数量 */
    private BigDecimal amount;

    /**
     * 统计日期（库列名为 date，MyBatis 查询需别名：{@code date as statisticsDate}）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime statisticsDate;

    private Integer status;

    /** 挂号人数 */
    private Long registrationNum;

    /** 已执行检查数量（库列 excute_check_amount） */
    private BigDecimal excuteCheckAmount;

    /** 已执行检验数量（库列 excute_test_amount） */
    private BigDecimal excuteTestAmount;

    /** 已执行处置数量（库列 excute_disposition_amount） */
    private BigDecimal excuteDispositionAmount;

    private Integer type;

    private Long deptId;

    /** 已执行数量 */
    private Long excuteNum;
}
