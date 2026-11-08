package com.aquaai.accountability.infrastructure.config;

import com.aquaai.accountability.domain.service.IncidentDeduplicationEngine;
import com.aquaai.accountability.domain.service.IncidentSlaCalculator;
import com.aquaai.accountability.domain.service.IncidentStateMachine;
import com.aquaai.accountability.domain.service.IndependentVerificationEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountabilityDomainConfig {

    @Bean
    public IncidentStateMachine incidentStateMachine() {
        return new IncidentStateMachine();
    }

    @Bean
    public IncidentSlaCalculator incidentSlaCalculator() {
        return new IncidentSlaCalculator();
    }

    @Bean
    public IndependentVerificationEngine independentVerificationEngine() {
        return new IndependentVerificationEngine();
    }

    @Bean
    public IncidentDeduplicationEngine incidentDeduplicationEngine() {
        return new IncidentDeduplicationEngine();
    }
}
