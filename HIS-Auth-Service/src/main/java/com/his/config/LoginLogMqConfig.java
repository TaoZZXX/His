package com.his.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoginLogMqConfig {

    public static final String LOGIN_LOG_EXCHANGE = "his.auth.login-log.exchange";
    public static final String LOGIN_LOG_QUEUE = "his.auth.login-log.queue";
    public static final String LOGIN_LOG_ROUTING_KEY = "his.auth.login-log";
    public static final String OPERATION_LOG_QUEUE = "his.auth.operation-log.queue";
    public static final String OPERATION_LOG_ROUTING_KEY = "his.auth.operation-log";
    public static final String DEAD_LETTER_EXCHANGE = "his.auth.log.dlx";
    public static final String DEAD_LETTER_QUEUE = "his.auth.log.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "his.auth.log.dead";

    @Bean
    public DirectExchange loginLogExchange() {
        return new DirectExchange(LOGIN_LOG_EXCHANGE, true, false);
    }

    @Bean
    public Queue loginLogQueue() {
        return new Queue(LOGIN_LOG_QUEUE, true, false, false, queueArgs());
    }

    @Bean
    public Binding loginLogBinding(@Qualifier("loginLogQueue") Queue loginLogQueue,
                                   DirectExchange loginLogExchange) {
        return BindingBuilder.bind(loginLogQueue).to(loginLogExchange).with(LOGIN_LOG_ROUTING_KEY);
    }

    @Bean
    public Queue operationLogQueue() {
        return new Queue(OPERATION_LOG_QUEUE, true, false, false, queueArgs());
    }

    @Bean
    public Binding operationLogBinding(@Qualifier("operationLogQueue") Queue operationLogQueue,
                                       DirectExchange loginLogExchange) {
        return BindingBuilder.bind(operationLogQueue).to(loginLogExchange).with(OPERATION_LOG_ROUTING_KEY);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Map<String, Object> queueArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        return args;
    }
}
