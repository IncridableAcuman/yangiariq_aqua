package com.aquaai.accountability.infrastructure.adapter;

import com.aquaai.accountability.domain.port.AccountabilityNetworkPort;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountabilityNetworkAdapter implements AccountabilityNetworkPort {

    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterUserAssociationRepository wuaRepository;

    @Override
    public Optional<String> getCanalName(UUID canalId) {
        if (canalId == null) {
            return Optional.empty();
        }
        return edgeRepository.findById(canalId).map(WaterNetworkEdge::getName);
    }

    @Override
    public Optional<String> getWuaName(UUID wuaId) {
        if (wuaId == null) {
            return Optional.empty();
        }
        return wuaRepository.findById(wuaId).map(WaterUserAssociation::getName);
    }
}
