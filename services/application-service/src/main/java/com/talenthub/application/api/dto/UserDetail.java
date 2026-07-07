package com.talenthub.application.api.dto;

import lombok.Setter;

public record UserDetail(
        String userId,
        String email,
        String username,
        String name
) {
}
