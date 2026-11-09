package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.application.service.SyncOrchestrationService;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TriggerDataSourceSyncUseCase {

    private final DataSourceRepository dataSourceRepository;
    private final SyncOrchestrationService syncOrchestrationService;

    @Transactional
    public DataSourceDto execute(UUID id) {
        DataSource source = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource", id));

        syncOrchestrationService.triggerManualSync(id);

        DataSource refreshed = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource", id));
        return DataSourceDto.from(refreshed);
    }
}
