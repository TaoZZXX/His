package com.his.service;

import com.his.config.LoginLogMqConfig;
import com.his.domain.SmsLoginLog;
import com.his.dto.LoginLogEvent;
import com.his.mapper.SmsLoginLogMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginLogEventListener {

    private static final Set<String> PROCESSED_IDS = ConcurrentHashMap.newKeySet();
    private final SmsLoginLogMapper smsLoginLogMapper;

    public LoginLogEventListener(SmsLoginLogMapper smsLoginLogMapper) {
        this.smsLoginLogMapper = smsLoginLogMapper;
    }

    @RabbitListener(queues = LoginLogMqConfig.LOGIN_LOG_QUEUE)
    public void onMessage(LoginLogEvent event, Message message) {
        if (event == null) {
            return;
        }
        String messageId = message == null ? null : message.getMessageProperties().getMessageId();
        if (messageId != null && !PROCESSED_IDS.add(messageId)) {
            return;
        }
        SmsLoginLog log = new SmsLoginLog();
        log.setUserId(event.getUserId());
        log.setIp(event.getIp());
        log.setCreateTime(event.getCreateTime());
        smsLoginLogMapper.insert(log);
    }
}
