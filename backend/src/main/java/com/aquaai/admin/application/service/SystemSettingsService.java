package com.aquaai.admin.application.service;

import com.aquaai.admin.application.dto.AdminDtos.SystemSettingsResponse;
import com.aquaai.shared.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final JwtProperties jwtProperties;

    @Value("${spring.application.name:YANGIARIQ AQUA AI}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${app.sensor.provider:simulated}")
    private String sensorProvider;

    @Value("${app.rate-limiting.limit-per-minute:120}")
    private int rateLimitPerMinute;

    public SystemSettingsResponse getSystemSettings() {
        long accessMinutes = jwtProperties.getAccessTokenExpirationMs() / 60000;
        long refreshDays = jwtProperties.getRefreshTokenExpirationMs() / (1000 * 60 * 60 * 24);

        return new SystemSettingsResponse(
                applicationName,
                activeProfile,
                "v0.8.20.1",
                accessMinutes,
                refreshDays,
                sensorProvider.toUpperCase(),
                "OPEN-METEO / COMPUTE_FALLBACK",
                true,
                rateLimitPerMinute,
                true,
                true,
                Instant.now()
        );
    }
}
