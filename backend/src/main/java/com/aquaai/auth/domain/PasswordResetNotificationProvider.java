package com.aquaai.auth.domain;

import java.time.Instant;

/**
 * Domain port for sending password reset notifications (e.g. Email, SMS, Telegram).
 * Decoupled from specific messaging providers.
 */
public interface PasswordResetNotificationProvider {

    /**
     * Sends password reset verification token to the recipient identifier.
     *
     * @param recipientIdentifier email or phone of the user requesting password reset
     * @param rawToken            the unhashed single-use reset token
     * @param expiresAt           timestamp when the token will expire
     */
    void sendPasswordResetNotification(String recipientIdentifier, String rawToken, Instant expiresAt);
}
