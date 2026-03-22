-- 发票号序列表：与业务事务同事务递增，保证唯一、连续（按库内顺序）
-- 说明：HIS-System 启动时 BmsInvoiceSequenceBootstrap 会自动执行等价 DDL；本文件供 DBA/离线环境备用。
CREATE TABLE IF NOT EXISTS bms_invoice_sequence (
    seq_name      VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '序列名',
    current_value BIGINT        NOT NULL COMMENT '当前已分配的最大值'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票号序列表';

-- 默认全局序列；起始值可按院方要求调整（建议 ≥1 且唯一递增）
INSERT INTO bms_invoice_sequence (seq_name, current_value)
VALUES ('GLOBAL', 100000000001)
ON DUPLICATE KEY UPDATE seq_name = seq_name;
