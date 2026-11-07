package com.aquaai.wateraccounting.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountingSensorPort {

    BigDecimal getCanalFlowRateM3S(UUID edgeId);

    boolean isSensorDataSimulated(UUID edgeId);
}
