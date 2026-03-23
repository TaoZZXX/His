package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DmsNonDrugModel {
    private Long id;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 模板名称 */
    private String name;

    /** 关联非药品项目ID列表（逗号分隔） */
    private String nonDrugIdList;

    /** 适用范围（1-个人 2-科室 3-全院） */
    private Integer scope;

    /** 所属人/科室ID */
    private Long ownId;

    /** 模板用途说明 */
    private String aim;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 模板编码 */
    private String code;

    /** 模板类型（1-检查模板 2-治疗模板等） */
    private Integer type;
}

