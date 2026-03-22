package com.his.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 发票号落在 {@link com.his.domain.BmsInvoiceRecord}，但并发下不能用「MAX(invoice_no)+1」安全发号（连接池 + 事务竞态）。
 * 使用独立序列表 + LAST_INSERT_ID 递增；启动时自动建表并写入默认行，无需手工执行 SQL。
 */
@Component
public class BmsInvoiceSequenceBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BmsInvoiceSequenceBootstrap.class);
    private static final long DEFAULT_START = 100000000001L;

    private static final String DDL = ""
            + "CREATE TABLE IF NOT EXISTS bms_invoice_sequence ("
            + "seq_name VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '序列名',"
            + "current_value BIGINT NOT NULL COMMENT '当前已分配的最大值'"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票号序列表'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(DDL);
            jdbcTemplate.update(
                    "INSERT IGNORE INTO bms_invoice_sequence (seq_name, current_value) VALUES (?, ?)",
                    "GLOBAL",
                    DEFAULT_START
            );
            log.info("bms_invoice_sequence 已就绪（自动建表或已存在）");
        } catch (Exception e) {
            log.error("bms_invoice_sequence 初始化失败（请检查库账号是否有 CREATE TABLE 权限，或手工执行 document/sql/bms_invoice_sequence.sql）: {}",
                    e.getMessage(), e);
        }
    }
}
