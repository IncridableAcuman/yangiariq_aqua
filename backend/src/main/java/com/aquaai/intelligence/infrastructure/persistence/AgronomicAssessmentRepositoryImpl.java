package com.aquaai.intelligence.infrastructure.persistence;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.port.AgronomicAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AgronomicAssessmentRepositoryImpl implements AgronomicAssessmentRepository {

    private static final Instant DEFAULT_MIN_INSTANT = Instant.parse("2020-01-01T00:00:00Z");

    private final SpringDataAgronomicAssessmentRepository springDataRepository;
    private final AgronomicAssessmentJpaMapper mapper;

    @Override
    @Transactional
    public AgronomicAssessment save(AgronomicAssessment assessment) {
        if (assessment == null) return null;
        AgronomicAssessmentJpaEntity entity = mapper.toEntity(assessment);
        AgronomicAssessmentJpaEntity saved = springDataRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgronomicAssessment> findLatestByFieldId(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return springDataRepository.findFirstByFieldIdOrderByAssessedAtDesc(fieldId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgronomicAssessment> findLatestByFieldIdIn(java.util.Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) return Collections.emptyList();
        return springDataRepository.findLatestByFieldIdIn(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgronomicAssessment> findByFieldId(UUID fieldId, Instant from, Instant to, int page, int size) {
        if (fieldId == null) return Collections.emptyList();

        Instant effectiveFrom = from != null ? from : DEFAULT_MIN_INSTANT;
        Instant effectiveTo = to != null ? to : Instant.now().plus(1, ChronoUnit.DAYS);

        int boundedPage = Math.max(0, page);
        int boundedSize = Math.clamp(size, 1, 100);

        PageRequest pageRequest = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "assessedAt"));
        Page<AgronomicAssessmentJpaEntity> resultPage = springDataRepository.findByFieldIdAndAssessedAtBetween(
                fieldId,
                effectiveFrom,
                effectiveTo,
                pageRequest
        );

        return resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFieldId(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null) return 0;
        Instant effectiveFrom = from != null ? from : DEFAULT_MIN_INSTANT;
        Instant effectiveTo = to != null ? to : Instant.now().plus(1, ChronoUnit.DAYS);
        return springDataRepository.countByFieldIdAndAssessedAtBetween(fieldId, effectiveFrom, effectiveTo);
    }
}
