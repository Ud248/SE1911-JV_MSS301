package com.talenthub.job.infrastructure.messaging;

import com.talenthub.events.JobCreatedEvent;
import com.talenthub.job.domain.aggregate.JobAggregate;
import com.talenthub.job.domain.exception.JobNotFoundException;
import com.talenthub.job.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobSagaListener {
    private final RabbitTemplate rabbitTemplate;
    private final JobRepository jobRepository;

    @RabbitListener(bindings = {@QueueBinding(value = @Queue(name = "job.application-created", durable = "true"),
                    exchange = @Exchange(name = "talenthub.events", type = "topic"), key = "job.created.increment")})
    public void updateCountApplicant(JobCreatedEvent jobCreatedEvent) {
        Optional<JobAggregate> optional = jobRepository.findById(jobCreatedEvent.jobId());

        JobAggregate jobAggregate = optional.orElseThrow(() -> {
            throw new JobNotFoundException(jobCreatedEvent.jobId());
        });

        jobAggregate.incrementApplicantCount();

        jobRepository.save(jobAggregate);
    }

}
