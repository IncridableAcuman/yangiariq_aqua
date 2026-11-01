package com.aquaai.recommendation.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Builds structured, zero-PII prompts for the advisory LLM.
 * Constrains the model with strict regional agricultural safety boundaries
 * and enforces a deterministic JSON output format.
 */
@Component
public class AgronomicPromptBuilder {

    public String buildPrompt(AgronomicRecommendationContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are an expert agronomic decision-support advisor for the arid, irrigated agricultural oasis of Yangiariq district, Khorezm region, Uzbekistan.\n");
        sb.append("Analyze the provided field, telemetry, anomaly, water, and weather data to generate a single actionable recommendation.\n\n");

        sb.append("CRITICAL SAFETY AND REGIONAL INVARIANTS:\n");
        sb.append("1. HYDRAULIC SAFETY INTERLOCK: If there is an active PIPE_BURST or extreme PRESSURE DROP, you MUST NOT recommend irrigating! You must recommend immediate pipe inspection and repair.\n");
        sb.append("2. REGIONAL ARIDITY & SALINITY: Yangiariq soils are alluvial and sandy/loam with high secondary salinization risk. Irrigation water is derived from the Amu Darya canal network. Salinity (EC > 4.0 dS/m) requires salt leaching fraction.\n");
        sb.append("3. NO PII: Never hallucinate or output farmer names, personal phone numbers, or private credentials.\n");
        sb.append("4. EVIDENCE-BASED: Base every conclusion strictly on the numbers and status flags provided in the context.\n");
        sb.append("5. STRICT JSON OUTPUT ONLY: Output ONLY a valid JSON object matching the required schema. No markdown formatting, no code fences (```json), no introductory text.\n\n");

        sb.append("REQUIRED JSON SCHEMA:\n");
        sb.append("{\n");
        sb.append("  \"recommendationType\": \"IRRIGATE_NOW\" | \"DELAY_IRRIGATION\" | \"REDUCE_WATER_VOLUME\" | \"AVOID_IRRIGATION\" | \"INSPECT_PIPE_BURST\" | \"INSPECT_SENSOR\" | \"MONITOR_SOIL_MOISTURE\" | \"MANAGE_SALINITY\" | \"PREPARE_HEAT_STRESS\" | \"PREPARE_FROST\" | \"INVESTIGATE_OVERCONSUMPTION\",\n");
        sb.append("  \"priority\": \"CRITICAL\" | \"HIGH\" | \"MEDIUM\" | \"LOW\" | \"INFO\",\n");
        sb.append("  \"title\": \"Short concise title (max 255 chars)\",\n");
        sb.append("  \"summary\": \"1-2 sentence executive summary (max 500 chars)\",\n");
        sb.append("  \"explanation\": \"Detailed agronomic explanation citing specific metrics\",\n");
        sb.append("  \"actions\": [\"Mandatory action step 1\", \"Mandatory action step 2\"],\n");
        sb.append("  \"doNotActions\": [\"Explicit DO NOT action to avoid crop/infrastructure damage\"],\n");
        sb.append("  \"limitations\": \"Provisional AI advisory statement and hardware constraints\",\n");
        sb.append("  \"confidenceScore\": 0.85\n");
        sb.append("}\n\n");

        sb.append("FIELD CONTEXT:\n");
        sb.append("- Parcel ID: ").append(context.getFieldId()).append("\n");
        sb.append("- Parcel Name: ").append(context.getFieldName()).append("\n");
        sb.append("- Crop: ").append(context.getCropType() != null ? context.getCropType() : "UNKNOWN").append("\n");
        sb.append("- Soil Type: ").append(context.getSoilType() != null ? context.getSoilType() : "UNKNOWN").append("\n");
        sb.append("- Area: ").append(context.getFieldAreaHectares() != null ? context.getFieldAreaHectares() + " ha" : "UNKNOWN").append("\n");
        sb.append("- Irrigation Method: ").append(context.getIrrigationMethod() != null ? context.getIrrigationMethod() : "UNKNOWN").append("\n\n");

        sb.append("TELEMETRY SENSORS:\n");
        sb.append("- Soil Moisture: ").append(context.getSoilMoisturePercent() != null ? context.getSoilMoisturePercent() + "%" : "UNAVAILABLE")
                .append(" (Quality: ").append(context.getSoilMoistureQuality() != null ? context.getSoilMoistureQuality() : "UNKNOWN").append(")\n");
        sb.append("- Water Flow: ").append(context.getWaterFlowM3H() != null ? context.getWaterFlowM3H() + " m3/h" : "UNAVAILABLE").append("\n");
        sb.append("- Water Pressure: ").append(context.getWaterPressureBar() != null ? context.getWaterPressureBar() + " bar" : "UNAVAILABLE").append("\n");
        sb.append("- Water Salinity EC: ").append(context.getSalinityDsM() != null ? context.getSalinityDsM() + " dS/m" : "UNAVAILABLE").append("\n");
        sb.append("- Ambient Temperature: ").append(context.getAmbientTemperatureC() != null ? context.getAmbientTemperatureC() + " °C" : "UNAVAILABLE").append("\n");
        sb.append("- Ambient Humidity: ").append(context.getAmbientHumidityPercent() != null ? context.getAmbientHumidityPercent() + "%" : "UNAVAILABLE").append("\n\n");

        sb.append("ACTIVE ANOMALIES:\n");
        if (context.getActiveAnomalies() == null || context.getActiveAnomalies().isEmpty()) {
            sb.append("None detected.\n\n");
        } else {
            String anomalies = context.getActiveAnomalies().stream()
                    .map(a -> String.format("- Type: %s, Severity: %s, Description: %s",
                            a.getAnomalyType(), a.getSeverity(), a.getDescription()))
                    .collect(Collectors.joining("\n"));
            sb.append(anomalies).append("\n\n");
        }

        sb.append("WATER ALLOCATION & SCHEDULING:\n");
        sb.append("- Scheduled Irrigation In Progress: ").append(context.isScheduledIrrigationActive()).append("\n");
        sb.append("- Delivered Volume Past 7 Days: ").append(context.getRecentWaterDeliveredM3() != null ? context.getRecentWaterDeliveredM3() + " m3" : "0 m3").append("\n\n");

        if (context.getWeather() != null) {
            sb.append("METEOROLOGICAL CONDITIONS:\n");
            sb.append("- Temperature: ").append(context.getWeather().getTemperature()).append(" °C\n");
            sb.append("- Relative Humidity: ").append(context.getWeather().getRelativeHumidity()).append("%\n");
            sb.append("- Precipitation Expected: ").append(context.getWeather().getPrecipitationAmountMm()).append(" mm (Prob: ")
                    .append(context.getWeather().getPrecipitationProbability()).append(")\n");
            sb.append("- Wind Speed: ").append(context.getWeather().getWindSpeedMps()).append(" m/s\n");
        }

        return sb.toString();
    }
}
