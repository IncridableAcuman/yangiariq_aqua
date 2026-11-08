package com.aquaai.performance.infrastructure.config;

import com.aquaai.performance.domain.service.BaselineComparabilityEngine;
import com.aquaai.performance.domain.service.ImpactAttributionEngine;
import com.aquaai.performance.domain.service.IndicatorCalculationEngine;
import com.aquaai.performance.domain.service.PerformanceEvaluationEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PerformanceDomainConfig {

    @Bean
    public IndicatorCalculationEngine indicatorCalculationEngine() {
        return new IndicatorCalculationEngine();
    }

    @Bean
    public BaselineComparabilityEngine baselineComparabilityEngine() {
        return new BaselineComparabilityEngine();
    }

    @Bean
    public ImpactAttributionEngine impactAttributionEngine() {
        return new ImpactAttributionEngine();
    }

    @Bean
    public PerformanceEvaluationEngine performanceEvaluationEngine(
            IndicatorCalculationEngine calculationEngine,
            BaselineComparabilityEngine comparabilityEngine
    ) {
        return new PerformanceEvaluationEngine(calculationEngine, comparabilityEngine);
    }
}
