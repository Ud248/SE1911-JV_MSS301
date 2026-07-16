package com.talenthub.job.config;

import com.talenthub.job.domain.aggregate.JobAggregate;
import com.talenthub.job.domain.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final JobRepository jobRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String defaultJobTitle = "Software Engineer (Java)";
        if (!jobRepository.isExisted(defaultJobTitle)) {
            log.info("Seeding Job data...");
            UUID departmentId = UUID.randomUUID();
            JobAggregate job = JobAggregate.createJob(
                    defaultJobTitle,
                    "Develop and maintain Java applications using Spring Boot and Microservices architecture.",
                    departmentId,
                    new BigDecimal("1000"),
                    new BigDecimal("2000"),
                    LocalDate.now().plusDays(30)
            );
            // Approve and publish the job so candidate can apply
            job.submitForApproval();
            job.approve();
            job.publish();
            
            jobRepository.save(job);
            log.info("Job seeded with ID: {}", job.getId());
        } else {
            log.info("Job data already seeded.");
        }
    }
}
