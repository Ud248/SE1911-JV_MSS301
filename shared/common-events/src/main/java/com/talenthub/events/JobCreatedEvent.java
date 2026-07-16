package com.talenthub.events;


import java.util.UUID;

public record JobCreatedEvent(
        UUID applicationId,
        UUID jobId,
        UUID candidateId,
        String cvFileUrl) {
}
