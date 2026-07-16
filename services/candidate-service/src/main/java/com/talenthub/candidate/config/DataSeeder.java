package com.talenthub.candidate.config;

import com.talenthub.candidate.domain.repository.CandidateRepository;
import com.talenthub.candidate.domain.model.Candidate;
import com.talenthub.candidate.domain.model.ContactInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CandidateRepository candidateRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String defaultEmail = "nguyenvana@example.com";
        if (!candidateRepository.existsByEmail(defaultEmail)) {
            log.info("Seeding Candidate data...");
            ContactInfo contact = new ContactInfo(defaultEmail, "0901234567", "Ho Chi Minh City");
            Candidate candidate = Candidate.register("Nguyen Van A", contact);
            candidateRepository.save(candidate);
            log.info("Candidate seeded with ID: {}", candidate.getId());
        } else {
            log.info("Candidate data already seeded.");
        }
    }
}
