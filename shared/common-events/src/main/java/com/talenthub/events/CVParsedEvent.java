package com.talenthub.events;

import java.util.UUID;

public record CVParsedEvent(
        UUID eventId,
        UUID applicationId,
        UUID candidateId,
        String fullName,
        String email,
        String phone,
        String summary          // tom tat ket qua parse
) {
}
