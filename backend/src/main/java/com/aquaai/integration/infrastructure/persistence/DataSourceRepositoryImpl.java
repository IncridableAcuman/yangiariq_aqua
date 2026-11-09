package com.aquaai.integration.infrastructure.persistence;

import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.infrastructure.persistence.entity.DataSourceJpaEntity;
import com.aquaai.integration.infrastructure.persistence.mapper.DataSourceJpaMapper;
import com.aquaai.integration.infrastructure.persistence.repository.SpringDataDataSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DataSourceRepositoryImpl implements DataSourceRepository {

    private final SpringDataDataSourceRepository springDataRepository;

    @Override
    public DataSource save(DataSource dataSource) {
        DataSourceJpaEntity entity = DataSourceJpaMapper.toEntity(dataSource);
        DataSourceJpaEntity saved = springDataRepository.save(entity);
        return DataSourceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<DataSource> findById(UUID id) {
        return springDataRepository.findById(id).map(DataSourceJpaMapper::toDomain);
    }

    @Override
    public Optional<DataSource> findBySourceCode(String sourceCode) {
        return springDataRepository.findBySourceCode(sourceCode).map(DataSourceJpaMapper::toDomain);
    }

    @Override
    public List<DataSource> findAll() {
        return springDataRepository.findAll().stream().map(DataSourceJpaMapper::toDomain).toList();
    }

    @Override
    public List<DataSource> findAllByStatus(DataSourceStatus status) {
        return springDataRepository.findByStatus(status).stream().map(DataSourceJpaMapper::toDomain).toList();
    }

    @Override
    public List<DataSource> findAllBySourceType(DataSourceType sourceType) {
        return springDataRepository.findBySourceType(sourceType).stream().map(DataSourceJpaMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }
}
