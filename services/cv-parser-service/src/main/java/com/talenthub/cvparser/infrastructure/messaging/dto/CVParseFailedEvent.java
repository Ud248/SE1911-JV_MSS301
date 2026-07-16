package com.talenthub.cvparser.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVParseFailedEvent {
    private UUID applicationId;
    private UUID jobId; // Optional, might need to be passed if job service needs it, but we don't have it in JobSlotReservedEvent.
    // Wait, JobSlotReservedEvent didn't include jobId. If CVParseFailed needs jobId to rollback, we MUST include jobId in JobSlotReservedEvent.
    private String reason;
}
