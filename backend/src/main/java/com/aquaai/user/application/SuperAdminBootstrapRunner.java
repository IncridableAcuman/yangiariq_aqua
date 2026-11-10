package com.aquaai.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runner that executes early during application startup (Order 5)
 * to evaluate and trigger Super Administrator bootstrap if conditions are met.
 */
@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class SuperAdminBootstrapRunner implements ApplicationRunner {

    private final SuperAdminBootstrapService bootstrapService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Evaluating Super Administrator bootstrap initialization status...");
        bootstrapService.bootstrapIfRequired();
    }
}
