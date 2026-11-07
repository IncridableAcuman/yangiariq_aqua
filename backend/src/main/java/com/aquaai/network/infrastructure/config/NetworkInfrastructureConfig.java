package com.aquaai.network.infrastructure.config;

import com.aquaai.network.domain.service.CanalDispatchOptimizer;
import com.aquaai.network.domain.service.HydraulicResilienceEvaluator;
import com.aquaai.network.domain.service.NetworkCapacityEngine;
import com.aquaai.network.domain.service.NetworkTopologyValidator;
import com.aquaai.network.domain.service.PathResolutionEngine;
import com.aquaai.network.domain.service.RegionalDemandAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkInfrastructureConfig {

    @Bean
    public NetworkTopologyValidator networkTopologyValidator() {
        return new NetworkTopologyValidator();
    }

    @Bean
    public PathResolutionEngine pathResolutionEngine() {
        return new PathResolutionEngine();
    }

    @Bean
    public NetworkCapacityEngine networkCapacityEngine() {
        return new NetworkCapacityEngine();
    }

    @Bean
    public RegionalDemandAggregator regionalDemandAggregator() {
        return new RegionalDemandAggregator();
    }

    @Bean
    public CanalDispatchOptimizer canalDispatchOptimizer() {
        return new CanalDispatchOptimizer();
    }

    @Bean
    public HydraulicResilienceEvaluator hydraulicResilienceEvaluator() {
        return new HydraulicResilienceEvaluator();
    }
}
