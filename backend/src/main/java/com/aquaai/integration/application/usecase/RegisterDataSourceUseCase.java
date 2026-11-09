package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.application.dto.RegisterDataSourceCommand;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterDataSourceUseCase {

    private final DataSourceRepository dataSourceRepository;

    @Transactional
    public DataSourceDto execute(RegisterDataSourceCommand command) {
        if (dataSourceRepository.findBySourceCode(command.sourceCode().trim().toUpperCase()).isPresent()) {
            throw new BusinessRuleException(
                    "Data source with code '" + command.sourceCode() + "' already exists",
                    "DUPLICATE_DATA_SOURCE_CODE"
            );
        }

        DataSource source = DataSource.create(
                command.sourceCode(),
                command.name(),
                command.sourceType(),
                command.providerName(),
                command.description(),
                command.endpointUrl(),
                command.configurationRef(),
                command.initialStatus()
        );

        DataSource saved = dataSourceRepository.save(source);
        return DataSourceDto.from(saved);
    }
}
