package com.his.service;

import com.his.config.LoginLogMqConfig;
import com.his.dto.LoginLogEvent;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoginLogEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public LoginLogEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(LoginLogEvent event) {
        MessagePostProcessor addMessageId = message -> {
            message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            return message;
        };

        rabbitTemplate.convertAndSend(
                LoginLogMqConfig.LOGIN_LOG_EXCHANGE,
                LoginLogMqConfig.LOGIN_LOG_ROUTING_KEY,
                event,
                addMessageId
        );
    }
}
