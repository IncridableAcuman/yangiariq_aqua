package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListDataSourcesUseCase {

    private final DataSourceRepository dataSourceRepository;

    @Transactional(readOnly = true)
    public List<DataSourceDto> execute() {
        return dataSourceRepository.findAll().stream()
                .map(DataSourceDto::from)
                .toList();
    }
}
