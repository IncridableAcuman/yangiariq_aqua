package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.port.IncidentVerificationRepository;
import com.aquaai.accountability.infrastructure.persistence.mapper.AccountabilityJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IncidentVerificationRepositoryImpl implements IncidentVerificationRepository {

    private final SpringDataIncidentVerificationRepository springDataRepository;
    private final AccountabilityJpaMapper mapper;

    @Override
    public IncidentVerification save(IncidentVerification verification) {
        var entity = mapper.toEntity(verification);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<IncidentVerification> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<IncidentVerification> findByIncidentId(UUID incidentId) {
        return springDataRepository.findByIncidentIdOrderByVerifiedAtDesc(incidentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
