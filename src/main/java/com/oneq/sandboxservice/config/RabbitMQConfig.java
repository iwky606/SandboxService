package com.oneq.sandboxservice.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    @Bean
    Queue judgeQueue(AmqpAdmin amqpAdmin) {
        Queue lazyQueue = QueueBuilder.durable("judge.queue").withArgument("x-queue-mode", "lazy").build();
        amqpAdmin.declareQueue(lazyQueue);
        return lazyQueue;
    }

    @Bean
    Queue resultJudgeQueue(AmqpAdmin amqpAdmin) {
        Queue lazyQueue = QueueBuilder.durable("result.judge.queue").withArgument("x-queue-mode", "lazy").build();
        amqpAdmin.declareQueue(lazyQueue);
        return lazyQueue;
    }

}

