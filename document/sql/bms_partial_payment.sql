-- =============================================================================
-- 部分付款：应收明细 + 收款流水 + 分摊
-- 查询「哪些已付」：bms_payable_item.status=1 或 paid_amount >= amount
-- 查询「付过几次、每次多少」：bms_payment_record + bms_payment_allocation
-- =============================================================================

CREATE TABLE IF NOT EXISTS `bms_payable_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `registration_id` bigint(20) NOT NULL COMMENT '挂号ID',
  `item_type` tinyint(4) NOT NULL COMMENT '费用类型：1非药品(检查检验) 2成药处方头 3草药处方头 4挂号/号别 5其它',
  `source_id` bigint(20) NOT NULL COMMENT '来源业务主键，如 dms_non_drug_item_record.id / dms_medicine_prescription_record.id',
  `item_name` varchar(200) DEFAULT NULL COMMENT '展示名称（冗余，便于列表）',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应收金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已收累计',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0未付清 1已付清',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reg_type_src` (`registration_id`,`item_type`,`source_id`),
  KEY `idx_registration_status` (`registration_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号维度可收费明细（部分付款粒度）';

CREATE TABLE IF NOT EXISTS `bms_payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `registration_id` bigint(20) NOT NULL COMMENT '挂号ID',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '本次实收合计',
  `pay_time` datetime DEFAULT NULL COMMENT '收款时间',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '收费员/操作人',
  `pay_method` tinyint(4) DEFAULT NULL COMMENT '支付方式：1现金 2微信 3支付宝 4银行卡 9其它',
  `remark` varchar(300) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_registration_time` (`registration_id`,`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款流水（一次收银可对应多条分摊）';

CREATE TABLE IF NOT EXISTS `bms_payment_allocation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payment_id` bigint(20) NOT NULL COMMENT 'bms_payment_record.id',
  `payable_item_id` bigint(20) NOT NULL COMMENT 'bms_payable_item.id',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '从本笔收款中摊到该明细的金额',
  PRIMARY KEY (`id`),
  KEY `idx_payment` (`payment_id`),
  KEY `idx_payable` (`payable_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款分摊明细（审计：每笔款对应哪些项目）';
