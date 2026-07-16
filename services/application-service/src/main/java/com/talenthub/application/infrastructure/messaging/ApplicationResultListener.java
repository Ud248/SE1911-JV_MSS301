package com.talenthub.application.infrastructure.messaging;

import com.talenthub.application.domain.model.Application;
import com.talenthub.application.domain.model.PipelineStage;
import com.talenthub.application.domain.repository.ApplicationRepository;
import com.talenthub.application.infrastructure.messaging.dto.CVParsedEvent;
import com.talenthub.application.infrastructure.messaging.dto.JobSlotReleasedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationResultListener {

    private final ApplicationRepository applicationRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "application.job.slot.released.queue", durable = "true"),
            exchange = @Exchange(value = "talenthub.events", type = "topic"),
            key = "job.slot.released"
    ))
    @Transactional
    public void onJobSlotReleased(JobSlotReleasedEvent event) {
        log.info("Received JobSlotReleasedEvent for applicationId: {}", event.getApplicationId());
        
        // C2: Update status to REJECTED due to CV error (Complete Compensation)
        Application app = applicationRepository.findById(event.getApplicationId()).orElseThrow(() -> new RuntimeException("App not found"));
        app.advanceStage(PipelineStage.REJECTED);
        app.addNote("SYSTEM", "Rejected: " + event.getReason());
        applicationRepository.save(app);
        
        log.info("Application {} updated to REJECTED due to Job Slot release/rejection.", app.getId());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "application.cv.parsed.success.queue", durable = "true"),
            exchange = @Exchange(value = "talenthub.events", type = "topic"),
            key = "cv.parsed.success"
    ))
    @Transactional
    public void onCvParsedSuccess(CVParsedEvent event) {
        log.info("Received CVParsedEvent for applicationId: {}", event.getApplicationId());
        
        // Advance to CV_SCREENING
        Application app = applicationRepository.findById(event.getApplicationId()).orElseThrow(() -> new RuntimeException("App not found"));
        if (app.getCurrentStage() == PipelineStage.NEW) {
            app.advanceStage(PipelineStage.CV_SCREENING);
            applicationRepository.save(app);
            log.info("Application {} advanced to CV_SCREENING.", app.getId());
        }
    }
}
