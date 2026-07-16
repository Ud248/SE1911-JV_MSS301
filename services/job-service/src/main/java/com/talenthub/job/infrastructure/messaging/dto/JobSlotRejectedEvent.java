package com.talenthub.job.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSlotRejectedEvent {
    private UUID applicationId;
    private String reason;
}
