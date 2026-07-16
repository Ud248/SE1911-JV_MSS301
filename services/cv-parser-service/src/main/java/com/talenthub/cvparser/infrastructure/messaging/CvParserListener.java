package com.talenthub.cvparser.infrastructure.messaging;

import com.talenthub.cvparser.infrastructure.messaging.dto.CVParseFailedEvent;
import com.talenthub.cvparser.infrastructure.messaging.dto.CVParsedEvent;
import com.talenthub.cvparser.infrastructure.messaging.dto.JobSlotReservedEvent;
import com.talenthub.cvparser.service.MockCvParserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvParserListener {

    private final MockCvParserService cvParsingService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cv.job.slot.reserved.queue", durable = "true"),
            exchange = @Exchange(value = "talenthub.events", type = "topic"),
            key = "job.slot.reserved"
    ))
    public void onJobSlotReserved(JobSlotReservedEvent event) {
        log.info("Received JobSlotReservedEvent for applicationId: {}, candidateId: {}", event.getApplicationId(), event.getCandidateId());

        try {
            // T3: Parse CV
            cvParsingService.processCvParsing(event.getCandidateId(), event.getCvFileUrl());
            
            // Success
            CVParsedEvent successEvent = new CVParsedEvent(event.getApplicationId());
            rabbitTemplate.convertAndSend("talenthub.events", "cv.parsed.success", successEvent);
            log.info("Successfully published CVParsedEvent for applicationId: {}", event.getApplicationId());
            
        } catch (Exception e) {
            // FAILURE: Publish error event to trigger Compensation
            CVParseFailedEvent failedEvent = new CVParseFailedEvent(
                event.getApplicationId(), 
                event.getJobId(), 
                "Lỗi phân tích file: " + e.getMessage()
            );
            rabbitTemplate.convertAndSend("talenthub.events", "cv.parsed.failed", failedEvent);
            log.error("Published CVParseFailedEvent for applicationId: {}. Reason: {}", event.getApplicationId(), e.getMessage());
        }
    }
}
