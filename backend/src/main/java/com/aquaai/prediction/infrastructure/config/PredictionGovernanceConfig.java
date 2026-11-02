package com.aquaai.prediction.infrastructure.config;

import com.aquaai.prediction.domain.service.BacktestingEngine;
import com.aquaai.prediction.domain.service.CalibrationProposalGenerator;
import com.aquaai.prediction.domain.service.ControlledRolloutManager;
import com.aquaai.prediction.domain.service.RegressionGuardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PredictionGovernanceConfig {

    @Bean
    public RegressionGuardService regressionGuardService() {
        return new RegressionGuardService();
    }

    @Bean
    public BacktestingEngine backtestingEngine(RegressionGuardService regressionGuardService) {
        return new BacktestingEngine(regressionGuardService);
    }

    @Bean
    public CalibrationProposalGenerator calibrationProposalGenerator() {
        return new CalibrationProposalGenerator();
    }

    @Bean
    public ControlledRolloutManager controlledRolloutManager() {
        return new ControlledRolloutManager();
    }
}
