package com.aquaai.user.domain.event;

import java.util.UUID;

public record UserSecurityInvalidationEvent(
        UUID userId,
        String reason
) {
}
