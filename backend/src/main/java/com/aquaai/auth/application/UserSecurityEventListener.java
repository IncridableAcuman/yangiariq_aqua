package com.aquaai.auth.application;

import com.aquaai.auth.domain.RefreshTokenSessionRepository;
import com.aquaai.user.domain.event.UserSecurityInvalidationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSecurityEventListener {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    @EventListener
    @Transactional
    public void onUserSecurityInvalidation(UserSecurityInvalidationEvent event) {
        log.info("Revoking active sessions for user {} (reason: {})", event.userId(), event.reason());
        refreshTokenSessionRepository.revokeAllActiveSessionsForUser(event.userId());
    }
}
