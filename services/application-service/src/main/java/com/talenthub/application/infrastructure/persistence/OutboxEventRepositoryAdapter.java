package com.talenthub.application.infrastructure.persistence;

import com.talenthub.application.domain.model.OutboxEvent;
import com.talenthub.application.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public List<OutboxEvent> findUnProcessEvents() {
        return outboxEventJpaRepository.findByUnProcessedEvents();
    }

    @Override
    public boolean existByAggregateId(UUID aggregateId) {
        return outboxEventJpaRepository.existsOutboxEventByAggregateId(aggregateId);
    }

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return outboxEventJpaRepository.save(outboxEvent);
    }
}
