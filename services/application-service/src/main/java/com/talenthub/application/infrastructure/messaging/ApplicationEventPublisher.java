package com.talenthub.application.infrastructure.messaging;

import com.talenthub.constants.RabbitMQConstants;
import com.talenthub.events.ApplicationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Publishing a new message to notification queue, with email={}", event.candidateEmail());

        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_NAME, RabbitMQConstants.ROUTE_KEY_NAME, event);

        log.info("Published a new message successful!");
    }
}
