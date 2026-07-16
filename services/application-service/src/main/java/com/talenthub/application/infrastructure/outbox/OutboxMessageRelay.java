package com.talenthub.application.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talenthub.application.domain.model.OutboxEvent;
import com.talenthub.application.domain.repository.OutboxEventRepository;
import com.talenthub.application.infrastructure.messaging.ApplicationEventPublisher;
import com.talenthub.constants.RabbitMQConstants;
import com.talenthub.events.ApplicationCreatedEvent;
import com.talenthub.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 10000)
    public void pollAndPublish() {

        List<OutboxEvent> outboxEvents = outboxEventRepository.findUnProcessEvents();

        outboxEvents.forEach(outboxEvent -> {
            try {
                String routingKey = outboxEvent.getEventType();

                switch (routingKey) {
                    case "application.created": {
                        eventPublisher.publishApplicationCreated(objectMapper.readValue(outboxEvent.getPayload(), ApplicationCreatedEvent.class));
                        outboxEvent.markProcessed();
                        outboxEventRepository.save(outboxEvent);
                        log.info("Published outbox event: type={}, aggregateId={}",
                                outboxEvent.getEventType(), outboxEvent.getAggregateId());
                        break;
                    }
                    case "job.created.increment": {
                        JobCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), JobCreatedEvent.class);
                        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_NAME, "job.created.increment", event);
                        outboxEvent.markProcessed();
                        outboxEventRepository.save(outboxEvent);
                        log.info("Published outbox event: type={}, aggregateId={}",
                                outboxEvent.getEventType(), outboxEvent.getAggregateId());
                        break;
                    }
                }
            } catch (Exception ex) {
                log.error("Exception in publish outbox event", ex);
            }

        });


    }
}
