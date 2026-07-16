package com.talenthub.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.talenthub.constants.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Bean
    public TopicExchange talenthubExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(RabbitMQConstants.NOTIFICATION_QUEUE)
                .deadLetterExchange(RabbitMQConstants.DLX_NAME)
                .build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange talenthubExchange) {
        return BindingBuilder.bind(notificationQueue).to(talenthubExchange).with(RabbitMQConstants.ROUTE_KEY_NAME);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter();
    }

    // ===== Dead Letter Infrastructure =====
    @Bean
    public FanoutExchange deadLetterExchange() {
        // Fanout vì DLX không cần routing: mọi message reject đều vào DLQ
        return new FanoutExchange(RabbitMQConstants.DLX_NAME, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DLX_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }
}
