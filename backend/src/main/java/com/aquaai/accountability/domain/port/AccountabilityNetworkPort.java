package com.aquaai.accountability.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface AccountabilityNetworkPort {

    Optional<String> getCanalName(UUID canalId);

    Optional<String> getWuaName(UUID wuaId);
}
