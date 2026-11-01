package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.IrrigationRequirement;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain service calculating volumetric crop water requirements, root-zone deficits,
 * evapotranspiration demand, rain offsets, and application efficiencies.
 * <p>
 * Scientific Principles:
 * - Net Irrigation Depth: I_net (mm) = max(0, Deficit_depth + ET_c - P_eff)
 * - Gross Irrigation Volume: V_gross (m³) = (I_net * 10 * Area_ha) / Efficiency
 * - Application Efficiency (eta): Drip ~0.90, Sprinkler ~0.75, Furrow ~0.60, Canal ~0.50
 */
public class IrrigationRequirementCalculator {

    private static final BigDecimal MM_HA_TO_M3 = BigDecimal.valueOf(10.0);

    public IrrigationRequirement calculate(
            BigDecimal currentMoisturePercent,
            BigDecimal targetMoisturePercent,
            String cropType,
            String soilType,
            String irrigationMethod,
            BigDecimal areaHectares,
            BigDecimal referenceEt0MmDay,
            BigDecimal forecastRainMm
    ) {
        BigDecimal area = (areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) > 0)
                ? areaHectares
                : BigDecimal.ONE;

        // 1. Resolve crop parameters: Kc and root depth (meters)
        BigDecimal cropKc = resolveCropKc(cropType);
        BigDecimal rootDepthM = resolveRootDepthMeters(cropType);

        // 2. Resolve default target moisture if not explicitly supplied
        BigDecimal targetMoisture = (targetMoisturePercent != null && targetMoisturePercent.compareTo(BigDecimal.ZERO) > 0)
                ? targetMoisturePercent
                : resolveDefaultTargetMoisture(cropType, soilType);

        // 3. Calculate soil moisture deficit depth in mm
        BigDecimal deficitDepthMm = BigDecimal.ZERO;
        if (currentMoisturePercent != null && currentMoisturePercent.compareTo(targetMoisture) < 0) {
            BigDecimal moistureGapFraction = targetMoisture.subtract(currentMoisturePercent)
                    .divide(BigDecimal.valueOf(100.0), 4, RoundingMode.HALF_UP);
            // Deficit mm = gap * rootDepth (m) * 1000 mm/m
            deficitDepthMm = moistureGapFraction.multiply(rootDepthM).multiply(BigDecimal.valueOf(1000.0));
        }

        // 4. Crop Evapotranspiration: ETc = Kc * ET0
        BigDecimal et0 = (referenceEt0MmDay != null && referenceEt0MmDay.compareTo(BigDecimal.ZERO) > 0)
                ? referenceEt0MmDay
                : BigDecimal.valueOf(5.0); // Seasonal default for Khorezm summer/spring
        BigDecimal etcMm = cropKc.multiply(et0).setScale(2, RoundingMode.HALF_UP);

        // 5. Effective Precipitation: Peff
        BigDecimal peffMm = BigDecimal.ZERO;
        if (forecastRainMm != null && forecastRainMm.compareTo(BigDecimal.valueOf(5.0)) > 0) {
            // First 5mm assumed lost to canopy interception and surface evaporation
            peffMm = forecastRainMm.subtract(BigDecimal.valueOf(5.0)).multiply(BigDecimal.valueOf(0.80));
        }

        // 6. Net Irrigation Requirement: I_net = max(0, Deficit + ETc - Peff)
        BigDecimal netRequirementMm = deficitDepthMm.add(etcMm).subtract(peffMm);
        if (netRequirementMm.compareTo(BigDecimal.ZERO) < 0) {
            netRequirementMm = BigDecimal.ZERO;
        }

        // 7. Resolve irrigation method efficiency and nominal flow rate
        BigDecimal efficiency = resolveIrrigationEfficiency(irrigationMethod);
        BigDecimal flowRateM3H = resolveNominalFlowRate(irrigationMethod, area);

        // 8. Gross Volumetric Requirement: V_gross (m³) = (I_net * 10 * Area) / Efficiency
        BigDecimal grossVolumeM3 = BigDecimal.ZERO;
        int durationMinutes = 0;
        if (netRequirementMm.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal netVolumeM3 = netRequirementMm.multiply(MM_HA_TO_M3).multiply(area);
            grossVolumeM3 = netVolumeM3.divide(efficiency, 4, RoundingMode.HALF_UP);

            // Duration (hours) = V_gross / FlowRate
            BigDecimal durationHours = grossVolumeM3.divide(flowRateM3H, 4, RoundingMode.HALF_UP);
            durationMinutes = (int) Math.round(durationHours.multiply(BigDecimal.valueOf(60.0)).doubleValue());
            // Cap at 1440 minutes (24 hours) for single-day window safety
            durationMinutes = Math.min(durationMinutes, 1440);
        }

        return new IrrigationRequirement(
                deficitDepthMm,
                netRequirementMm,
                grossVolumeM3,
                durationMinutes,
                flowRateM3H,
                efficiency
        );
    }

    private BigDecimal resolveCropKc(String cropType) {
        if (cropType == null) {
            return BigDecimal.valueOf(0.85);
        }
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(0.95);
            case "WHEAT" -> BigDecimal.valueOf(0.85);
            case "RICE" -> BigDecimal.valueOf(1.15);
            case "MAIZE" -> BigDecimal.valueOf(0.90);
            case "VEGETABLE" -> BigDecimal.valueOf(0.80);
            case "MELON" -> BigDecimal.valueOf(0.75);
            default -> BigDecimal.valueOf(0.85);
        };
    }

    private BigDecimal resolveRootDepthMeters(String cropType) {
        if (cropType == null) {
            return BigDecimal.valueOf(0.60);
        }
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(0.80);
            case "WHEAT" -> BigDecimal.valueOf(0.60);
            case "RICE" -> BigDecimal.valueOf(0.40);
            case "MAIZE" -> BigDecimal.valueOf(0.70);
            case "VEGETABLE" -> BigDecimal.valueOf(0.40);
            case "MELON" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.valueOf(0.60);
        };
    }

    private BigDecimal resolveDefaultTargetMoisture(String cropType, String soilType) {
        // Clay holds water tightly (higher FC), sandy drains quickly (lower FC)
        BigDecimal baseTarget = BigDecimal.valueOf(70.0);
        if (soilType != null) {
            String upper = soilType.toUpperCase();
            if (upper.contains("SANDY")) {
                baseTarget = BigDecimal.valueOf(65.0);
            } else if (upper.contains("CLAY")) {
                baseTarget = BigDecimal.valueOf(75.0);
            }
        }
        if (cropType != null && cropType.equalsIgnoreCase("RICE")) {
            baseTarget = BigDecimal.valueOf(85.0);
        }
        return baseTarget;
    }

    private BigDecimal resolveIrrigationEfficiency(String method) {
        if (method == null) {
            return BigDecimal.valueOf(0.70);
        }
        return switch (method.toUpperCase()) {
            case "DRIP" -> BigDecimal.valueOf(0.90);
            case "SPRINKLER" -> BigDecimal.valueOf(0.75);
            case "FURROW" -> BigDecimal.valueOf(0.60);
            case "CANAL", "DITCH" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.valueOf(0.65);
        };
    }

    private BigDecimal resolveNominalFlowRate(String method, BigDecimal areaHectares) {
        BigDecimal base = BigDecimal.valueOf(40.0);
        if (method != null) {
            base = switch (method.toUpperCase()) {
                case "DRIP" -> BigDecimal.valueOf(25.0);
                case "SPRINKLER" -> BigDecimal.valueOf(35.0);
                case "FURROW" -> BigDecimal.valueOf(50.0);
                case "CANAL" -> BigDecimal.valueOf(75.0);
                default -> BigDecimal.valueOf(40.0);
            };
        }
        // Scale proportionally by area with a reasonable clamp
        BigDecimal scaled = base.multiply(areaHectares).setScale(2, RoundingMode.HALF_UP);
        return scaled.max(BigDecimal.valueOf(10.0)).min(BigDecimal.valueOf(250.0));
    }
}
