package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.WeatherSnapshot;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain port for estimating reference evapotranspiration (ET0).
 * <p>
 * Full scientific standard: FAO-56 Penman-Monteith requires net solar radiation,
 * soil heat flux, and wind speed at 2m height. Where specialized meteorological
 * sensors are absent, empirical temperature-based approximations (e.g. Hargreaves)
 * are utilized with documented limitations.
 */
public interface EvapotranspirationCalculator {

    BigDecimal calculateReferenceET(WeatherSnapshot weather, Instant date);

    String getMethodName();

    boolean isScientificallyValidated();
}
