package com.his.domain;

import lombok.Data;

/**
 * 草药模板明细项表（dms_herbal_model_item）
 */
@Data
public class DmsHerbalModelItem {

    private Long id;

    /** 状态（0 禁用 / 1 启用） */
    private Integer status;

    /** 脚注/备注 */
    private String footnote;

    /** 药品 ID（草药） */
    private Long drugId;

    /** 用量数值 */
    private Long usageNum;

    /** 用量单位 */
    private Integer usageNumUnit;

    /** 关联模板 ID */
    private Long modelId;
}
