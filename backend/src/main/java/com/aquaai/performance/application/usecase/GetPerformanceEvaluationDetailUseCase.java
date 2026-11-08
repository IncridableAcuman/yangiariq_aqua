package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPerformanceEvaluationDetailUseCase {

    private final PerformanceEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public PerformanceEvaluation execute(UUID id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Performance Evaluation not found: " + id));
    }
}
