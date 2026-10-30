package com.aquaai.auth.infrastructure;

import com.aquaai.auth.domain.PasswordResetNotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MockPasswordResetNotificationProvider implements PasswordResetNotificationProvider {

    // In-memory token store for testing and verification in non-production environments
    private final Map<String, StoredResetToken> testTokenRegistry = new ConcurrentHashMap<>();

    @Override
    public void sendPasswordResetNotification(String recipientIdentifier, String rawToken, Instant expiresAt) {
        log.info("Mock password reset notification dispatched for recipient: {}", maskIdentifier(recipientIdentifier));

        // Store token for test inspection without logging raw secret
        testTokenRegistry.put(recipientIdentifier.toLowerCase().trim(), new StoredResetToken(rawToken, expiresAt));
    }

    /**
     * Testing hook to retrieve the raw token for integration test assertions.
     */
    public Optional<String> getLatestTokenForTesting(String recipientIdentifier) {
        StoredResetToken stored = testTokenRegistry.get(recipientIdentifier.toLowerCase().trim());
        if (stored != null && Instant.now().isBefore(stored.expiresAt())) {
            return Optional.of(stored.token());
        }
        return Optional.empty();
    }

    public void clearTestingRegistry() {
        testTokenRegistry.clear();
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) {
            return "***";
        }
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }

    private record StoredResetToken(String token, Instant expiresAt) {
    }
}
