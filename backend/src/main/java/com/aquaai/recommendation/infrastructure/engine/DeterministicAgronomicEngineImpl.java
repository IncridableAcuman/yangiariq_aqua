package com.aquaai.recommendation.infrastructure.engine;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.RecommendationEvidence;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.model.RecommendedAction;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.AgronomicRecommendationEngine;
import com.aquaai.recommendation.domain.service.ConfidenceCalculator;
import com.aquaai.sensor.domain.model.ReadingQuality;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic Agronomic Recommendation Engine.
 * Evaluates physics-based, hydraulic, and environmental decision rules grounded
 * in Yangiariq district agricultural conditions.
 */
@Component
public class DeterministicAgronomicEngineImpl implements AgronomicRecommendationEngine {

    private final ConfidenceCalculator confidenceCalculator = new ConfidenceCalculator();

    // Agronomic operational thresholds (provisional engineering baselines)
    private static final BigDecimal MOISTURE_CRITICAL_DEFICIT = BigDecimal.valueOf(15.0);
    private static final BigDecimal MOISTURE_HIGH_DEFICIT = BigDecimal.valueOf(18.0);
    private static final BigDecimal MOISTURE_SATURATION = BigDecimal.valueOf(35.0);
    private static final BigDecimal SALINITY_HAZARD = BigDecimal.valueOf(4.0);
    private static final BigDecimal RAINFALL_SIGNIFICANT_MM = BigDecimal.valueOf(15.0);
    private static final BigDecimal HEAT_EXTREME_C = BigDecimal.valueOf(40.0);
    private static final BigDecimal FROST_RISK_C = BigDecimal.valueOf(2.0);

    @Override
    public List<AgronomicRecommendation> evaluate(AgronomicRecommendationContext context) {
        if (context == null || context.getFieldId() == null) {
            return List.of();
        }

        List<AgronomicRecommendation> candidates = new ArrayList<>();
        UUID fieldId = context.getFieldId();
        Instant now = context.getEvaluationTime() != null ? context.getEvaluationTime() : Instant.now();
        Instant validUntil = now.plusSeconds(86400); // 24-hour default validity

        // -------------------------------------------------------------
        // Rule 1: Hydraulic Pipeline / Pipe Burst Safety Interlock (CRITICAL)
        // -------------------------------------------------------------
        boolean hasPipeBurst = context.getActiveAnomalies().stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.SUSPECTED_PIPE_BURST
                        || a.getAnomalyType() == AnomalyType.WATER_PRESSURE_DROP);

        if (hasPipeBurst) {
            List<RecommendationEvidence> evidence = new ArrayList<>();
            evidence.add(RecommendationEvidence.of("ANOMALY", "HYDRAULIC_SAFETY",
                    context.getWaterPressureBar(), BigDecimal.valueOf(1.2), "bar",
                    "Aktiv gidravlik anomaliya aniqlangan (quvur yorilishi yoki bosim tushishi)"));

            if (context.getWaterFlowM3H() != null) {
                evidence.add(RecommendationEvidence.of("TELEMETRY", "WATER_FLOW",
                        context.getWaterFlowM3H(), BigDecimal.valueOf(30.0), "m3/h",
                        "Quvur tarmog'idagi suv sarfi ko'rsatkichi"));
            }

            List<RecommendedAction> actions = List.of(
                    RecommendedAction.of(1, "Zudlik bilan dala quvur tarmog'i va asosiy tarqatuvchi bo'g'inni ko'zdan kechiring", "IMMEDIATE"),
                    RecommendedAction.of(2, "Nasos stantsiyasi yoki kirish zulfinini yopib, gidravlik qochqinni bartaraf eting", "IMMEDIATE"),
                    RecommendedAction.doNot(3, "Quvur butunligi tiklanmaguncha sug'orish nasosini qayta yoqmang")
            );

            BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.92));

            candidates.add(AgronomicRecommendation.create(
                    fieldId,
                    RecommendationType.INSPECT_PIPE_BURST,
                    RecommendationPriority.CRITICAL,
                    "Avariya xavfi: Quvur tarmog'i va gidravlik bosimni tekshirish",
                    "Gidravlik bosim pasayishi yoki quvur yorilishi ehtimoli aniqlandi. Asosiy suv quvurlarini tekshirish zarur.",
                    "Daladagi bosim yoki oqim datchiklari g'ayritabiiy gidravlik o'zgarishni qayd etgan. Avariya bartaraf etilmasdan sug'orish davom ettirilsa, katta miqdorda suv isrofi va tuproq yuvilishi yuzaga keladi.",
                    confidence,
                    now,
                    now.plusSeconds(28800), // 8h window for emergency
                    RecommendationSource.DETERMINISTIC_ENGINE,
                    "Gidravlik avariya holatida agronomik sug'orish tavsiyalari vaqtincha to'xtatiladi.",
                    context.isSimulated(),
                    evidence,
                    actions
            ));

            // Hard safety rule: When pipe burst is suspected, do NOT generate irrigation recommendations
            return candidates;
        }

        // -------------------------------------------------------------
        // Rule 2: Expected Significant Rainfall Delay
        // -------------------------------------------------------------
        WeatherSnapshot weather = context.getWeather();
        boolean rainfallExpected = weather != null && weather.hasSignificantRainfall(RAINFALL_SIGNIFICANT_MM);
        boolean moistureIsCritical = context.getSoilMoisturePercent() != null
                && context.getSoilMoisturePercent().compareTo(MOISTURE_CRITICAL_DEFICIT) <= 0;

        if (rainfallExpected && !moistureIsCritical) {
            List<RecommendationEvidence> evidence = List.of(
                    RecommendationEvidence.of("WEATHER_FORECAST", "PRECIPITATION",
                            weather.getPrecipitationAmountMm(), RAINFALL_SIGNIFICANT_MM, "mm",
                            "Yaqin 24-48 soat ichida kutilayotgan yog'in miqdori: " + weather.getPrecipitationAmountMm() + " mm"),
                    RecommendationEvidence.of("TELEMETRY", "SOIL_MOISTURE",
                            context.getSoilMoisturePercent(), MOISTURE_CRITICAL_DEFICIT, "%",
                            "Joriy tuproq namligi kritik darajadan yuqori")
            );

            List<RecommendedAction> actions = List.of(
                    RecommendedAction.of(1, "Rejalashtirilgan sug'orishni 24-48 soatga kechiktiring", "IMMEDIATE"),
                    RecommendedAction.of(2, "Yog'ingarchilikdan so'ng tuproq namligi dinamikasini datchiklar orqali qayta tekshiring", "WITHIN_24H"),
                    RecommendedAction.doNot(3, "Yog'ingarchilik oldidan ortiqcha suv berib, ildiz chirishi va botqoqlanishga yo'l qo'ymang")
            );

            BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.85));

            candidates.add(AgronomicRecommendation.create(
                    fieldId,
                    RecommendationType.DELAY_IRRIGATION,
                    RecommendationPriority.HIGH,
                    "Yog'ingarchilik kutilayotganligi sababli sug'orishni kechiktirish",
                    "Yaqin 24-48 soatda " + weather.getPrecipitationAmountMm() + " mm yog'ingarchilik kutilmoqda. Sug'orishni kechiktirish suvni tejaydi.",
                    "Hozirgi tuproq namligi o'simlikning kritik so'lish nuqtasida emas. Kutilayotgan tabiiy yog'in ekin ehtiyojini qondirish uchun yetarli bo'lib, sun'iy suv berish me'yordan ortiq namlik va suv isrofiga sabab bo'ladi.",
                    confidence,
                    now,
                    validUntil,
                    RecommendationSource.DETERMINISTIC_ENGINE,
                    "Prognoz ob-havo xizmati ma'lumotlariga tayanadi; atmosfera o'zgarishlari kuzatib borilishi lozim.",
                    context.isSimulated(),
                    evidence,
                    actions
            ));
        }

        // -------------------------------------------------------------
        // Rule 3: Soil Moisture Deficit / Irrigate Now
        // -------------------------------------------------------------
        if (context.getSoilMoisturePercent() != null && context.getSoilMoistureQuality() == ReadingQuality.VALID) {
            BigDecimal moisture = context.getSoilMoisturePercent();

            if (moisture.compareTo(MOISTURE_CRITICAL_DEFICIT) <= 0) {
                // Critical deficit
                List<RecommendationEvidence> evidence = new ArrayList<>();
                evidence.add(RecommendationEvidence.of("TELEMETRY", "SOIL_MOISTURE",
                        moisture, MOISTURE_CRITICAL_DEFICIT, "%",
                        "Tuproq namligi kritik chegaradan (" + MOISTURE_CRITICAL_DEFICIT + "%) past: " + moisture + "%"));

                if (context.getSoilMoistureTrend1h() != null) {
                    evidence.add(RecommendationEvidence.of("TELEMETRY", "MOISTURE_TREND",
                            context.getSoilMoistureTrend1h(), BigDecimal.ZERO, "%/h",
                            "Soatlik namlik pasayish sur'ati: " + context.getSoilMoistureTrend1h() + "%/soat"));
                }

                List<RecommendedAction> actions = List.of(
                        RecommendedAction.of(1, "Zudlik bilan navbatdagi sug'orish siklini faollashtiring (afzalligi tungi vaqt)", "IMMEDIATE"),
                        RecommendedAction.of(2, "Tomchilatib yoki egatlab sug'orish tarmog'ida suv oqimini me'yorda taqsimlang", "IMMEDIATE"),
                        RecommendedAction.doNot(3, "Quyosh qizigan vaqtda (11:00-16:00) sug'orish qilmang, bug'lanish va barg kuyishi xavfi mavjud")
                );

                BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.90));

                candidates.add(AgronomicRecommendation.create(
                        fieldId,
                        RecommendationType.IRRIGATE_NOW,
                        RecommendationPriority.CRITICAL,
                        "Kritik namlik tanqisligi: Shoshilinch sug'orish zarur",
                        "Tuproq namligi " + moisture + "% ga tushib ketgan. Ekin so'lish nuqtasiga yaqinlashmoqda.",
                        "Ildiz qatlamidagi volumetric namlik kritik minimal me'yordan past. O'simlik vegetatsiyasida stress yuzaga kelmasligi uchun zudlik bilan sug'orishni amalga oshirish lozim.",
                        confidence,
                        now,
                        validUntil,
                        RecommendationSource.DETERMINISTIC_ENGINE,
                        "O'lchov datchik o'rnatilgan chuqurlikdagi namlikka asoslangan.",
                        context.isSimulated(),
                        evidence,
                        actions
                ));
            } else if (moisture.compareTo(MOISTURE_HIGH_DEFICIT) <= 0 && !rainfallExpected) {
                // Moderate deficit
                List<RecommendationEvidence> evidence = List.of(
                        RecommendationEvidence.of("TELEMETRY", "SOIL_MOISTURE",
                                moisture, BigDecimal.valueOf(25.0), "%",
                                "Tuproq namligi quyi optimal chegarada: " + moisture + "%")
                );

                List<RecommendedAction> actions = List.of(
                        RecommendedAction.of(1, "Yaqin 24 soat ichida navbatdagi sug'orishni rejalashtiring", "WITHIN_24H"),
                        RecommendedAction.of(2, "Kanal va zulfinlardagi suv taqsimotini tayyorlab qo'ying", "ROUTINE")
                );

                BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.80));

                candidates.add(AgronomicRecommendation.create(
                        fieldId,
                        RecommendationType.IRRIGATE_NOW,
                        RecommendationPriority.HIGH,
                        "Rejali sug'orishni boshlash tavsiya etiladi",
                        "Tuproq namligi " + moisture + "%. Ekinning maqbul o'sishi uchun sug'orish tavsiya qilinadi.",
                        "Tuproq namligi optimal namlik (25-30%) oralig'idan pastga tusha boshlagan. Yaqin vaqtda sug'orish hosildorlikni barqaror ushlab turishga yordam beradi.",
                        confidence,
                        now,
                        validUntil,
                        RecommendationSource.DETERMINISTIC_ENGINE,
                        "Agrotexnik me'yorlar Yangiariq tumani iqlimiga moslashtirilgan.",
                        context.isSimulated(),
                        evidence,
                        actions
                ));
            } else if (moisture.compareTo(MOISTURE_SATURATION) >= 0) {
                // Soil saturation / Over-hydration
                List<RecommendationEvidence> evidence = List.of(
                        RecommendationEvidence.of("TELEMETRY", "SOIL_MOISTURE",
                                moisture, MOISTURE_SATURATION, "%",
                                "Tuproq namligi to'yinganlik chegarasidan yuqori: " + moisture + "%")
                );

                List<RecommendedAction> actions = List.of(
                        RecommendedAction.of(1, "Sug'orishni to'xtating va zulfinlarni yoping", "IMMEDIATE"),
                        RecommendedAction.of(2, "Dala zovur va drenaj tarmoqlarida suv oqimini nazorat qiling", "WITHIN_24H"),
                        RecommendedAction.doNot(3, "Tuproq namligi 25% dan pastga tushmaguncha qayta suv bermang")
                );

                BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.85));

                candidates.add(AgronomicRecommendation.create(
                        fieldId,
                        RecommendationType.AVOID_IRRIGATION,
                        RecommendationPriority.MEDIUM,
                        "Sug'orishni to'xtatish: Tuproq namligi me'yordan yuqori",
                        "Tuproq namligi " + moisture + "% ga yetgan. Qo'shimcha sug'orish ildiz chirishiga va suv isrofiga olib keladi.",
                        "Tuproq to'la nam sig'imi darajasiga yetgan. Ortiqcha suv ildiz zonasida kislorod yetishmasligi (asfiksiya) hamda sho'rlanishning ko'tarilishiga sabab bo'lishi mumkin.",
                        confidence,
                        now,
                        validUntil,
                        RecommendationSource.DETERMINISTIC_ENGINE,
                        "Dala drenaj holatini tekshirish tavsiya etiladi.",
                        context.isSimulated(),
                        evidence,
                        actions
                ));
            }
        }

        // -------------------------------------------------------------
        // Rule 4: Salinity Hazard Management
        // -------------------------------------------------------------
        if (context.getSalinityDsM() != null && context.getSalinityDsM().compareTo(SALINITY_HAZARD) >= 0) {
            List<RecommendationEvidence> evidence = List.of(
                    RecommendationEvidence.of("TELEMETRY", "SALINITY",
                            context.getSalinityDsM(), SALINITY_HAZARD, "dS/m",
                            "Tuproq/suv sho'rlanishi xavfli chegaradan yuqori: " + context.getSalinityDsM() + " dS/m")
            );

            List<RecommendedAction> actions = List.of(
                    RecommendedAction.of(1, "Zovur va kollektor kanallari ochiqligini ta'minlang", "IMMEDIATE"),
                    RecommendedAction.of(2, "Sho'r yuvish me'yoriga mos chuchuk suv taqsimotini rejalashtiring", "WITHIN_24H"),
                    RecommendedAction.doNot(3, "Mineral o'g'itlarni sho'r yuvilmasdan oldin qo'llashdan saqlaning")
            );

            BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.82));

            candidates.add(AgronomicRecommendation.create(
                    fieldId,
                    RecommendationType.MANAGE_SALINITY,
                    RecommendationPriority.HIGH,
                    "Sho'rlanish xavfi: Drenaj va sho'r yuvish nazorati",
                    "Sho'rlanish ko'rsatkichi " + context.getSalinityDsM() + " dS/m ga yetdi. Zovur oqimini tekshiring.",
                    "Yangiariq oazisi tuproqlarida sho'rlanishning oshishi ildiz orqali suv va ozuqa so'rilishini to'xtatadi. Zovurlar orqali minerallashgan suvni chiqarib yuborish lozim.",
                    confidence,
                    now,
                    validUntil,
                    RecommendationSource.DETERMINISTIC_ENGINE,
                    "Sho'rlanish datchigi va sug'orish suvi sifati laboratoriya tahlili bilan solishtirilishi lozim.",
                    context.isSimulated(),
                    evidence,
                    actions
            ));
        }

        // -------------------------------------------------------------
        // Rule 5: Extreme Atmospheric Heat Stress / Garmsel
        // -------------------------------------------------------------
        if (weather != null && weather.isExtremeHeat(HEAT_EXTREME_C)) {
            List<RecommendationEvidence> evidence = List.of(
                    RecommendationEvidence.of("WEATHER", "TEMPERATURE",
                            weather.getTemperature(), HEAT_EXTREME_C, "°C",
                            "Havo harorati kritik jazirama darajasida: " + weather.getTemperature() + " °C"),
                    RecommendationEvidence.of("WEATHER", "HUMIDITY",
                            weather.getRelativeHumidity(), BigDecimal.valueOf(15.0), "%",
                            "Nisbiy havo namligi past (quruq garmsel)")
            );

            List<RecommendedAction> actions = List.of(
                    RecommendedAction.of(1, "Kechki yoki tungi vaqtda yengil namlantirish (konditsion sug'orish) o'tkazing", "WITHIN_24H"),
                    RecommendedAction.doNot(2, "Kunduzgi soat 11:00 dan 17:00 gacha sug'orish qilmang")
            );

            BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.88));

            candidates.add(AgronomicRecommendation.create(
                    fieldId,
                    RecommendationType.PREPARE_HEAT_STRESS,
                    RecommendationPriority.HIGH,
                    "Garmsel va jazirama issiqqa agrotexnik tayyorgarlik",
                    "Kutilayotgan harorat " + weather.getTemperature() + " °C. Tungi namlantirish bug'lanish zararini kamaytiradi.",
                    "Xorazm iqlimida garmsel shamoli transpiratsiya sarfini 30% dan ko'proq oshiradi. Kunduzgi sug'orish barg kuyishiga olib kelishi sababli, amallarni faqat kechqurun bajarish lozim.",
                    confidence,
                    now,
                    validUntil,
                    RecommendationSource.DETERMINISTIC_ENGINE,
                    "Meteorologik sharoitlar o'zgarishi mumkin.",
                    context.isSimulated(),
                    evidence,
                    actions
            ));
        }

        // -------------------------------------------------------------
        // Rule 6: Sensor Reliability / Hardware Inspection
        // -------------------------------------------------------------
        boolean hasSensorIssue = context.getActiveAnomalies().stream()
                .anyMatch(a -> a.getAnomalyType() == AnomalyType.SENSOR_STUCK
                        || a.getAnomalyType() == AnomalyType.POSSIBLE_SENSOR_DRIFT
                        || a.getAnomalyType() == AnomalyType.SENSOR_OFFLINE);

        if (hasSensorIssue) {
            List<RecommendationEvidence> evidence = List.of(
                    RecommendationEvidence.of("ANOMALY", "SENSOR_HEALTH",
                            BigDecimal.ONE, BigDecimal.ZERO, "count",
                            "Datchikda qotib qolish, siljish yoki aloqa yo'qolishi qayd etilgan")
            );

            List<RecommendedAction> actions = List.of(
                    RecommendedAction.of(1, "Datchik elektr ta'minoti va tuproq bilan kontaktini tekshiring", "ROUTINE"),
                    RecommendedAction.of(2, "Datchik ko'rsatkichlarini qo'lda portativ namlik o'lchagich bilan solishtiring", "ROUTINE")
            );

            BigDecimal confidence = confidenceCalculator.calculate(context, evidence.size(), BigDecimal.valueOf(0.80));

            candidates.add(AgronomicRecommendation.create(
                    fieldId,
                    RecommendationType.INSPECT_SENSOR,
                    RecommendationPriority.MEDIUM,
                    "Telemetriya datchigi holatini tekshirish",
                    "Datchik telemetriyasida nosozlik belgilari aniqlandi. Uskunani ko'zdan kechirish tavsiya etiladi.",
                    "Datchik ko'rsatkichlarida noaniqlik bo'lsa, agronomik hisob-kitoblar xatolikka uchrashi mumkin. Hardware datchikni kalibrlash va tozalash tavsiya etiladi.",
                    confidence,
                    now,
                    validUntil,
                    RecommendationSource.DETERMINISTIC_ENGINE,
                    "Hardware diagnostikasi telemetriya tahliliga asoslangan.",
                    context.isSimulated(),
                    evidence,
                    actions
            ));
        }

        return candidates;
    }
}
