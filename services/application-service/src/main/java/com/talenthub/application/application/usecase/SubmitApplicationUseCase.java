package com.talenthub.application.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talenthub.application.application.command.SubmitApplicationCommand;
import com.talenthub.application.domain.exception.DuplicateApplicationException;
import com.talenthub.application.domain.exception.JobNotOpenForApplicationException;
import com.talenthub.application.domain.model.Application;
import com.talenthub.application.domain.model.OutboxEvent;
import com.talenthub.application.domain.repository.ApplicationRepository;
import com.talenthub.application.domain.repository.OutboxEventRepository;
import com.talenthub.application.infrastructure.client.candidate.CandidateServiceClient;
import com.talenthub.application.infrastructure.client.candidate.CandidateView;
import com.talenthub.application.infrastructure.client.job.JobServiceClient;
import com.talenthub.application.infrastructure.client.job.JobView;
import com.talenthub.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitApplicationUseCase {

    private final ApplicationRepository repo;
    private final JobServiceClient jobServiceClient;
    private final CandidateServiceClient candidateServiceClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UUID execute(SubmitApplicationCommand cmd) throws JsonProcessingException {

        JobView job = jobServiceClient.getJobById(cmd.jobId());

        if (!job.isPublished()) {
            throw new JobNotOpenForApplicationException(cmd.jobId(), " not yet published!");
        }

        if (job.isExprired(LocalDate.now())) {
            throw new JobNotOpenForApplicationException(cmd.jobId(), " were expired!");
        }

        // Validate candidate tồn tại (gọi candidate-service qua Eureka LB) - làm TRƯỚC
        // dup check
        // để mọi request đều thực sự gọi sang candidate-service.
        CandidateView candidate = candidateServiceClient.getCandidateById(cmd.candidateId());

        log.info("Candidate={} registering job={}", candidate.fullName(), cmd.jobId());

        // BRULE-09: chặn nộp trùng ở tầng application
        if (repo.existsByCandidateIdAndJobId(cmd.candidateId(), cmd.jobId())) {
            throw new DuplicateApplicationException(cmd.candidateId(), cmd.jobId());
        }

        Application application = Application.submit(cmd.candidateId(), cmd.jobId());
        Application saved = repo.save(application);


        // Step 2: Save to outbox_events table

        JobCreatedEvent jobCreatedEvent = new JobCreatedEvent(saved.getId(), cmd.jobId(), cmd.candidateId(), cmd.cvFileUrl());

        outboxEventRepository.save(OutboxEvent.create("Application", saved.getId(), "job.created.increment", objectMapper.writeValueAsString(jobCreatedEvent)));

//        outboxEventRepository.save(OutboxEvent.create("Application", saved.getId(), "application.created",
//                objectMapper.writeValueAsString(new ApplicationCreatedEvent(UUID.randomUUID(),
//                        saved.getId(), candidate.id(), job.id(), candidate.email(), candidate.fullName(), job.title(),
//                        cmd.cvFileUrl(), Instant.now()))));

        // // Publish a new message to notification queue
        // publisher.publishApplicationCreated(new
        // ApplicationCreatedEvent(UUID.randomUUID(),
        // saved.getId(), candidate.id(), job.id(), candidate.email(),
        // candidate.fullName(), job.title(), Instant.now()));

        return saved.getId();
    }
}