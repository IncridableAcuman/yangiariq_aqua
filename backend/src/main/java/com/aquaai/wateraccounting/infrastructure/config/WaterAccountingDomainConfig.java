package com.aquaai.wateraccounting.infrastructure.config;

import com.aquaai.wateraccounting.domain.service.AllocationComplianceEngine;
import com.aquaai.wateraccounting.domain.service.ConservationIntelligenceEngine;
import com.aquaai.wateraccounting.domain.service.ConveyanceLossEngine;
import com.aquaai.wateraccounting.domain.service.DistributionEquityEngine;
import com.aquaai.wateraccounting.domain.service.WaterBalanceEngine;
import com.aquaai.wateraccounting.domain.service.WaterReconciliationEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WaterAccountingDomainConfig {

    @Bean
    public WaterBalanceEngine waterBalanceEngine() {
        return new WaterBalanceEngine();
    }

    @Bean
    public ConveyanceLossEngine conveyanceLossEngine() {
        return new ConveyanceLossEngine();
    }

    @Bean
    public AllocationComplianceEngine allocationComplianceEngine() {
        return new AllocationComplianceEngine();
    }

    @Bean
    public DistributionEquityEngine distributionEquityEngine() {
        return new DistributionEquityEngine();
    }

    @Bean
    public ConservationIntelligenceEngine conservationIntelligenceEngine() {
        return new ConservationIntelligenceEngine();
    }

    @Bean
    public WaterReconciliationEngine waterReconciliationEngine() {
        return new WaterReconciliationEngine();
    }
}
