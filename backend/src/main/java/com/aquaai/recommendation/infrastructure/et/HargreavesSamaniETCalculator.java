package com.aquaai.recommendation.infrastructure.et;

import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.EvapotranspirationCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Empirical Reference Evapotranspiration (ET0) calculator utilizing the Hargreaves-Samani (1985) formula.
 * <p>
 * Formulation:
 * ET0 = 0.0023 * Ra * (Tmean + 17.8) * sqrt(Tmax - Tmin)
 * where Ra is extraterrestrial solar radiation expressed in equivalent water evaporation (mm/day).
 * <p>
 * Agronomic Caveat:
 * This empirical temperature-based approach provides an approximate baseline for arid Khorezm plains
 * when solar pyranometers and 2m wind sensors are unavailable. Full FAO-56 Penman-Monteith validation
 * requires complete meteorological station hardware.
 */
@Component
public class HargreavesSamaniETCalculator implements EvapotranspirationCalculator {

    // Yangiariq default latitude ~ 41.34 degrees North
    private static final double DEFAULT_LATITUDE_DEG = 41.34;
    private static final double SOLAR_CONSTANT_GSC = 0.0820; // MJ m^-2 min^-1

    @Override
    public BigDecimal calculateReferenceET(WeatherSnapshot weather, Instant date) {
        if (weather == null || weather.getTemperature() == null) {
            return BigDecimal.ZERO;
        }

        Instant evalDate = date != null ? date : (weather.getTimestamp() != null ? weather.getTimestamp() : Instant.now());
        ZonedDateTime zdt = evalDate.atZone(ZoneOffset.UTC);
        int dayOfYear = zdt.getDayOfYear();

        double tObs = weather.getTemperature().doubleValue();
        // Continental arid diurnal range estimation: day peak is ~3°C above current, night low is ~10°C below
        double tMax = tObs + 4.0;
        double tMin = tObs - 8.0;
        double tMean = (tMax + tMin) / 2.0;

        if (tMean <= -17.8) {
            return BigDecimal.ZERO;
        }

        double deltaT = Math.max(1.0, tMax - tMin);
        double raMmDay = calculateExtraterrestrialRadiationMm(DEFAULT_LATITUDE_DEG, dayOfYear);

        double et0 = 0.0023 * raMmDay * (tMean + 17.8) * Math.sqrt(deltaT);
        double clampedET0 = Math.max(0.0, Math.min(15.0, et0));

        return BigDecimal.valueOf(clampedET0).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates extraterrestrial radiation (Ra) in mm/day equivalent evaporation according to FAO-56.
     */
    private double calculateExtraterrestrialRadiationMm(double latitudeDeg, int dayOfYear) {
        double phi = Math.toRadians(latitudeDeg);
        double dr = 1.0 + 0.033 * Math.cos(2.0 * Math.PI * dayOfYear / 365.0);
        double delta = 0.409 * Math.sin((2.0 * Math.PI * dayOfYear / 365.0) - 1.39);

        double tanProduct = -Math.tan(phi) * Math.tan(delta);
        double omegaS = Math.acos(Math.max(-1.0, Math.min(1.0, tanProduct)));

        // Ra in MJ m^-2 day^-1
        double raMj = (24.0 * 60.0 / Math.PI) * SOLAR_CONSTANT_GSC * dr *
                (omegaS * Math.sin(phi) * Math.sin(delta) + Math.cos(phi) * Math.cos(delta) * Math.sin(omegaS));

        // Convert MJ m^-2 day^-1 to mm/day equivalent: 1 MJ m^-2 day^-1 = 0.408 mm/day
        return Math.max(0.0, raMj * 0.408);
    }

    @Override
    public String getMethodName() {
        return "HARGREAVES_SAMANI_1985";
    }

    @Override
    public boolean isScientificallyValidated() {
        return false;
    }
}
