package com.talenthub.job.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CVParseFailedEvent {
    private UUID applicationId;
    private UUID jobId;
    private String reason;
}
