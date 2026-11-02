package com.aquaai.analytics.application.service;

import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodResolverUnitTest {

    private PeriodResolver periodResolver;

    @BeforeEach
    void setUp() {
        periodResolver = new PeriodResolver();
    }

    @Test
    @DisplayName("Default period resolves to LAST_30_DAYS when null type is passed")
    void testNullPeriodDefaultsTo30Days() {
        PeriodResolver.PeriodWindow window = periodResolver.resolve(null, null, null);
        assertThat(window).isNotNull();
        assertThat(window.durationDays()).isBetween(29L, 31L);
        assertThat(window.label()).isNotEmpty();
    }

    @Test
    @DisplayName("TODAY period resolves to current day UTC start to now")
    void testTodayPeriod() {
        PeriodResolver.PeriodWindow window = periodResolver.resolve(PerformancePeriodType.TODAY, null, null);
        assertThat(window).isNotNull();
        assertThat(window.start()).isBeforeOrEqualTo(window.end());
        assertThat(window.durationDays()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("LAST_7_DAYS period resolves to 7 days duration")
    void testLast7DaysPeriod() {
        PeriodResolver.PeriodWindow window = periodResolver.resolve(PerformancePeriodType.LAST_7_DAYS, null, null);
        assertThat(window).isNotNull();
        assertThat(window.durationDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("LAST_90_DAYS period resolves to 90 days duration")
    void testLast90DaysPeriod() {
        PeriodResolver.PeriodWindow window = periodResolver.resolve(PerformancePeriodType.LAST_90_DAYS, null, null);
        assertThat(window).isNotNull();
        assertThat(window.durationDays()).isEqualTo(90);
    }

    @Test
    @DisplayName("SEASON period resolves to current agricultural season window")
    void testSeasonPeriod() {
        PeriodResolver.PeriodWindow window = periodResolver.resolve(PerformancePeriodType.SEASON, null, null);
        assertThat(window).isNotNull();
        assertThat(window.start()).isBeforeOrEqualTo(window.end());
        assertThat(window.durationDays()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("CUSTOM period with valid boundaries computes correct duration")
    void testCustomPeriodValid() {
        Instant from = Instant.now().minus(14, ChronoUnit.DAYS);
        Instant to = Instant.now();

        PeriodResolver.PeriodWindow window = periodResolver.resolve(PerformancePeriodType.CUSTOM, from, to);
        assertThat(window).isNotNull();
        assertThat(window.durationDays()).isEqualTo(14);
        assertThat(window.start()).isEqualTo(from);
        assertThat(window.end()).isEqualTo(to);
    }

    @Test
    @DisplayName("CUSTOM period throws BusinessRuleException when start is after end")
    void testCustomPeriodInvalidRange() {
        Instant from = Instant.now();
        Instant to = Instant.now().minus(5, ChronoUnit.DAYS);

        assertThatThrownBy(() -> periodResolver.resolve(PerformancePeriodType.CUSTOM, from, to))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    @DisplayName("CUSTOM period throws BusinessRuleException when range exceeds 365 days")
    void testCustomPeriodExceedsMaxDays() {
        Instant from = Instant.now().minus(400, ChronoUnit.DAYS);
        Instant to = Instant.now();

        assertThatThrownBy(() -> periodResolver.resolve(PerformancePeriodType.CUSTOM, from, to))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("365 days");
    }
}
