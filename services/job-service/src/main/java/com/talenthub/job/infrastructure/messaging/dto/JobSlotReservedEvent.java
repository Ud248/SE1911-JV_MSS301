package com.talenthub.job.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSlotReservedEvent {
    private UUID applicationId;
    private UUID jobId;
    private UUID candidateId;
    private String cvFileUrl;
}
