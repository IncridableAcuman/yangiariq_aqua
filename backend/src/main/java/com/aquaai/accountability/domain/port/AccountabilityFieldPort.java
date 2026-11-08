package com.aquaai.accountability.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface AccountabilityFieldPort {

    boolean isFieldOwnedBy(UUID fieldId, UUID userId);

    Optional<String> getFieldName(UUID fieldId);
}
