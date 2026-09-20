package com.his.service;

import com.his.config.LoginLogMqConfig;
import com.his.domain.SmsLoginLog;
import com.his.mapper.SmsLoginLogMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OperationLogEventListener {

    private static final Set<String> PROCESSED_IDS = ConcurrentHashMap.newKeySet();
    private final SmsLoginLogMapper smsLoginLogMapper;

    public OperationLogEventListener(SmsLoginLogMapper smsLoginLogMapper) {
        this.smsLoginLogMapper = smsLoginLogMapper;
    }

    @RabbitListener(queues = LoginLogMqConfig.OPERATION_LOG_QUEUE)
    public void onMessage(Map<String, Object> event, Message message) {
        if (event == null) {
            return;
        }
        String messageId = message == null ? null : message.getMessageProperties().getMessageId();
        if (messageId != null && !PROCESSED_IDS.add(messageId)) {
            return;
        }
        SmsLoginLog log = new SmsLoginLog();
        log.setUserId(toLong(event.get("userId")));
        log.setIp(event.get("ip") == null ? "OP|O|UNKNOWN /unknown|status=0|ip=unknown" : String.valueOf(event.get("ip")));
        log.setCreateTime(parseDateTime(event.get("createTime")));
        smsLoginLogMapper.insert(log);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(String.valueOf(value));
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }
}
