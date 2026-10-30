package com.aquaai.dashboard.application;

import com.aquaai.dashboard.api.dto.DistrictDashboardSummary;
import com.aquaai.field.domain.FieldRepository;
import com.aquaai.problem.domain.ProblemRepository;
import com.aquaai.problem.domain.ProblemStatus;
import com.aquaai.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final ProblemRepository problemRepository;

    @Transactional(readOnly = true)
    public DistrictDashboardSummary getSummary() {
        log.info("Computing read-model dashboard summary for Yangiariq district");

        long farmers = userRepository.count();
        long fields = fieldRepository.count();
        long activeProblems = problemRepository.countByStatus(ProblemStatus.OPEN);

        return DistrictDashboardSummary.builder()
                .district("Yangiariq")
                .totalRegisteredFarmers(farmers)
                .totalMonitoredFields(fields)
                .totalCultivatedHectares(BigDecimal.valueOf(1420.50))
                .totalWaterDistributedM3(BigDecimal.valueOf(48200.00))
                .activeWaterProblems(activeProblems)
                .activeSensorsOnline(24)
                .build();
    }
}
