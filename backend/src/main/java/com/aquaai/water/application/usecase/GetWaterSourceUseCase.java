package com.aquaai.water.application.usecase;

import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case: Retrieves details for a specific regional water source.
 * Open to all authenticated users.
 */
@Service
@RequiredArgsConstructor
public class GetWaterSourceUseCase {

    private final WaterSourceRepository waterSourceRepository;

    @Transactional(readOnly = true)
    public WaterSourceResponse execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("WaterSource", "null");
        }
        WaterSource source = waterSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterSource", id));
        return WaterSourceResponse.from(source);
    }
}
