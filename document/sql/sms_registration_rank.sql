-- 挂号号别（普通/专家等）及默认挂号费
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sms_registration_rank` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '号别编码，唯一',
  `name` varchar(64) NOT NULL COMMENT '号别名称',
  `seq_no` int(11) NOT NULL DEFAULT '0' COMMENT '排序，小在前',
  `price` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '挂号费',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号号别';

INSERT INTO `sms_registration_rank` (`code`, `name`, `seq_no`, `price`, `status`)
VALUES
  ('NORMAL', '普通', 10, 5.00, 1),
  ('EXPERT', '专家', 20, 50.00, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `price` = VALUES(`price`), `seq_no` = VALUES(`seq_no`), `status` = VALUES(`status`);
