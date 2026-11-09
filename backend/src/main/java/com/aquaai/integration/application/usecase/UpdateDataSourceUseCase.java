package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.application.dto.UpdateDataSourceCommand;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateDataSourceUseCase {

    private final DataSourceRepository dataSourceRepository;

    @Transactional
    public DataSourceDto execute(UUID id, UpdateDataSourceCommand command) {
        DataSource source = dataSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource", id));

        source.updateMetadata(
                command.name(),
                command.description(),
                command.endpointUrl(),
                command.configurationRef()
        );

        DataSource updated = dataSourceRepository.save(source);
        return DataSourceDto.from(updated);
    }
}
