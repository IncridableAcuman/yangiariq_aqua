package com.aquaai.network.domain.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface NetworkSensorPort {
    Optional<BigDecimal> getLatestPressureBar(UUID fieldId);
    Optional<BigDecimal> getLatestFlowM3H(UUID fieldId);
}
