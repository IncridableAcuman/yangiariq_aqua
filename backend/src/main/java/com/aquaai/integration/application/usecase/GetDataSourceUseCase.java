package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDataSourceUseCase {

    private final DataSourceRepository dataSourceRepository;

    @Transactional(readOnly = true)
    public DataSourceDto execute(UUID id) {
        return dataSourceRepository.findById(id)
                .map(DataSourceDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("DataSource", id));
    }
}
