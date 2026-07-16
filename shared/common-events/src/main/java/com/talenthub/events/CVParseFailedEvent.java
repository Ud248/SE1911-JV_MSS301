package com.talenthub.events;

import java.util.UUID;

public record CVParseFailedEvent(
        UUID eventId,
        UUID applicationId,
        UUID candidateId,
        String reason           // ly do that bai: "Unsupported format", "File corrupted"
) {
}
