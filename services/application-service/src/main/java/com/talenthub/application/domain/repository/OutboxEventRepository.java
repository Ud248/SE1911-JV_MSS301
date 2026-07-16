package com.talenthub.application.domain.repository;

import com.talenthub.application.domain.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository{
    List<OutboxEvent> findUnProcessEvents();
    boolean existByAggregateId(UUID aggregateId);
    OutboxEvent save(OutboxEvent outboxEvent);
}
