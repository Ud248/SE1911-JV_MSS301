package com.talenthub.application.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.talenthub.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
    public Binding notificationBinding(Exchange talenthubExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(talenthubExchange).with(RabbitMQConstants.ROUTE_KEY_NAME).noargs();
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter();
    }
}
