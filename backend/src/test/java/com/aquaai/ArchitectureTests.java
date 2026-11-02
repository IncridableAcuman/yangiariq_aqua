package com.aquaai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.aquaai");
    }

    @Test
    @DisplayName("1. Controllers must reside in api packages")
    void controllersShouldResideInApiPackage() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("2. Application services must reside in application packages")
    void applicationServicesShouldResideInApplicationPackage() {
        classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().areAnnotatedWith(Service.class)
                .and().resideInAPackage("com.aquaai..")
                .and().resideOutsideOfPackages("..infrastructure..", "..shared..")
                .should().resideInAPackage("..application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("3. Infrastructure classes must reside in infrastructure packages")
    void infrastructureClassesShouldResideInInfrastructurePackage() {
        classes()
                .that().haveSimpleNameEndingWith("Provider")
                .and().areNotInterfaces()
                .and().resideOutsideOfPackage("..shared..")
                .should().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("4. Controllers must not access repositories directly")
    void controllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("5. API layer must not expose persistence entities directly")
    void controllersMustNotExposeEntities() {
        methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..api..")
                .and().arePublic()
                .should().notHaveRawReturnType(Entity.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("6. Domain layer must not depend on infrastructure layer")
    void domainMustNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("7. AI domain and application contracts must not depend on Gemini SDK or vendor classes")
    void aiDomainAndApplicationMustNotDependOnGeminiSdk() {
        noClasses()
                .that().resideInAnyPackage("..ai.domain..", "..ai.application..")
                .should().dependOnClassesThat().resideInAnyPackage("com.google..", "org.springframework.ai..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("8. Sensor domain and application contracts must not depend on MQTT or hardware vendors")
    void sensorDomainAndApplicationMustNotDependOnMqttOrHardware() {
        noClasses()
                .that().resideInAnyPackage("..sensor.domain..", "..sensor.application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "org.springframework.integration.mqtt..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("9. Module controllers must have 'Controller' suffix")
    void controllersShouldHaveSuffix() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .check(importedClasses);
    }

    @Test
    @DisplayName("10. Field domain must not depend on JPA or persistence annotations")
    void fieldDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..field.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("11. Field domain must not depend on Spring MVC or PostgreSQL vendor classes")
    void fieldDomainMustNotDependOnWebOrPostgres() {
        noClasses()
                .that().resideInAPackage("..field.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("12. Field persistence entities must reside in field.infrastructure.persistence")
    void fieldPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..field..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..field.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("13. Water domain must not depend on JPA or persistence annotations")
    void waterDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..water.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("14. Water domain must not depend on Spring MVC or PostgreSQL vendor classes")
    void waterDomainMustNotDependOnWebOrPostgres() {
        noClasses()
                .that().resideInAPackage("..water.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("15. Water persistence entities must reside in water.infrastructure.persistence")
    void waterPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..water..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..water.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("16. Water domain must not depend on other business domain modules directly")
    void waterDomainMustNotDependOnOtherModules() {
        noClasses()
                .that().resideInAPackage("..water.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..field..", "..user..", "..auth..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("17. Water module must not depend on AI module")
    void waterMustNotDependOnAi() {
        noClasses()
                .that().resideInAPackage("..water..")
                .should().dependOnClassesThat().resideInAPackage("..ai..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("18. Water module must not depend on Sensor module")
    void waterMustNotDependOnSensor() {
        noClasses()
                .that().resideInAPackage("..water..")
                .should().dependOnClassesThat().resideInAPackage("..sensor..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("19. Water domain must not depend on Spring framework classes")
    void waterDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..water.domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("20. Sensor domain must not depend on JPA or persistence annotations")
    void sensorDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..sensor.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("21. Sensor domain must not depend on Spring MVC or PostgreSQL vendor classes")
    void sensorDomainMustNotDependOnWebOrPostgres() {
        noClasses()
                .that().resideInAPackage("..sensor.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("22. Sensor persistence entities must reside in sensor.infrastructure.persistence")
    void sensorPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..sensor..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..sensor.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("23. Sensor domain must not depend on Spring framework classes")
    void sensorDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..sensor.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("24. Sensor domain must not depend on other business domain modules directly")
    void sensorDomainMustNotDependOnOtherModules() {
        noClasses()
                .that().resideInAPackage("..sensor.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..water..", "..user..", "..auth..", "..field.domain..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("25. Anomaly domain must not depend on JPA or persistence annotations")
    void anomalyDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..anomaly.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("26. Anomaly domain must not depend on Spring MVC or PostgreSQL vendor classes")
    void anomalyDomainMustNotDependOnWebOrPostgres() {
        noClasses()
                .that().resideInAPackage("..anomaly.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("27. Anomaly persistence entities must reside in anomaly.infrastructure.persistence")
    void anomalyPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..anomaly..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..anomaly.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("28. Anomaly domain must not depend on Spring framework classes")
    void anomalyDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..anomaly.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("29. Recommendation domain must not depend on JPA or persistence annotations")
    void recommendationDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..recommendation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("30. Recommendation domain must not depend on Spring MVC or web classes")
    void recommendationDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..recommendation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("31. Recommendation persistence entities must reside in recommendation.infrastructure.persistence")
    void recommendationPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..recommendation..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..recommendation.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("32. Recommendation domain must not depend on Spring framework classes")
    void recommendationDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..recommendation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("33. Irrigation domain must not depend on JPA or persistence annotations")
    void irrigationDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..irrigation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("34. Irrigation domain must not depend on Spring MVC or web classes")
    void irrigationDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..irrigation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("35. Irrigation persistence entities must reside in irrigation.infrastructure.persistence")
    void irrigationPersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..irrigation..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..irrigation.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("36. Irrigation domain must not depend on Spring framework classes")
    void irrigationDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..irrigation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("37. Irrigation domain must not depend on other business domain modules directly")
    void irrigationDomainMustNotDependOnOtherModules() {
        noClasses()
                .that().resideInAPackage("..irrigation.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..water..", "..user..", "..auth..", "..field..", "..sensor..", "..anomaly..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("38. Intelligence domain must not depend on Spring framework classes")
    void intelligenceDomainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("39. Intelligence domain must not depend on JPA or persistence annotations")
    void intelligenceDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("40. Intelligence domain must not depend on PostgreSQL vendor classes")
    void intelligenceDomainMustNotDependOnPostgres() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.postgresql..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("41. Intelligence domain must not depend on Gemini SDK or vendor classes")
    void intelligenceDomainMustNotDependOnGemini() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("com.google..", "org.springframework.ai..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("42. Intelligence domain must not depend on HTTP or web classes")
    void intelligenceDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("43. Intelligence domain must not depend on sensor infrastructure")
    void intelligenceDomainMustNotDependOnSensorInfrastructure() {
        noClasses()
                .that().resideInAPackage("..intelligence.domain..")
                .should().dependOnClassesThat().resideInAPackage("..sensor.infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("44. Intelligence application must not depend on controllers")
    void intelligenceApplicationMustNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..intelligence.application..")
                .should().dependOnClassesThat().areAnnotatedWith(RestController.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("45. Intelligence persistence entities must reside in intelligence.infrastructure.persistence")
    void intelligencePersistenceEntitiesMustResideInInfrastructurePersistence() {
        classes()
                .that().resideInAPackage("..intelligence..")
                .and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..intelligence.infrastructure.persistence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("46. AI provider implementations must remain infrastructure adapters")
    void aiProviderImplementationsMustRemainInfrastructureAdapters() {
        classes()
                .that().haveSimpleNameEndingWith("AIAdapter").or().haveSimpleNameEndingWith("AIProvider")
                .and().areNotInterfaces()
                .should().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("47. Deterministic agronomic engines must not depend on AI providers directly")
    void deterministicEnginesMustNotDependOnAi() {
        noClasses()
                .that().haveSimpleNameContaining("Deterministic").or().haveSimpleNameContaining("AgronomicAssessmentEngine")
                .should().dependOnClassesThat().haveSimpleNameContaining("Gemini")
                .check(importedClasses);
    }

    @Test
    @DisplayName("48. Intelligence must not create circular dependency with irrigation")
    void intelligenceMustNotCreateCircularDependencyWithIrrigation() {
        noClasses()
                .that().resideInAPackage("..irrigation..")
                .should().dependOnClassesThat().resideInAPackage("..intelligence..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("49. Intelligence must not directly control hardware or actuator SDKs")
    void intelligenceMustNotControlHardware() {
        noClasses()
                .that().resideInAPackage("..intelligence..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "com.pi4j..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("50. Intelligence recommendation engine must synthesize assessments")
    void intelligenceRecommendationEngineMustSynthesizeAssessments() {
        classes()
                .that().haveSimpleName("IntelligenceRecommendationEngine")
                .should().dependOnClassesThat().haveSimpleName("AgronomicAssessment")
                .check(importedClasses);
    }

    @Test
    @DisplayName("51. Intelligence adapter must evaluate hydraulic safety interlock")
    void intelligenceAdapterMustEvaluateHydraulicSafetyInterlock() {
        classes()
                .that().haveSimpleName("IntelligenceHydraulicSafetyAdapter")
                .should().dependOnClassesThat().haveSimpleName("HydraulicSafetyInterlock")
                .check(importedClasses);
    }

    @Test
    @DisplayName("52. Dashboard controllers must reside in api package")
    void dashboardControllersMustResideInApiPackage() {
        classes()
                .that().resideInAPackage("..dashboard..")
                .and().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..dashboard.api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("53. Dashboard application services must reside in application package")
    void dashboardApplicationServicesMustResideInApplicationPackage() {
        classes()
                .that().resideInAPackage("..dashboard..")
                .and().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..dashboard.application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("54. Dashboard controllers must not access repositories directly")
    void dashboardControllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..dashboard.api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("55. Dashboard module must not depend on hardware or actuator SDKs")
    void dashboardMustNotDependOnHardware() {
        noClasses()
                .that().resideInAPackage("..dashboard..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "com.pi4j..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("56. Operations controllers must reside in api package")
    void operationsControllersMustResideInApiPackage() {
        classes()
                .that().resideInAPackage("..operations..")
                .and().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..operations.api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("57. Operations application services must reside in application package")
    void operationsApplicationServicesMustResideInApplicationPackage() {
        classes()
                .that().resideInAPackage("..operations..")
                .and().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..operations.application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("58. Operations controllers must not access repositories directly")
    void operationsControllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..operations.api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("59. Operations module must not depend on hardware or actuator SDKs")
    void operationsMustNotDependOnHardware() {
        noClasses()
                .that().resideInAPackage("..operations..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "com.pi4j..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("60. Analytics controllers must reside in api package")
    void analyticsControllersMustResideInApiPackage() {
        classes()
                .that().resideInAPackage("..analytics..")
                .and().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..analytics.api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("61. Analytics application services must reside in application package")
    void analyticsApplicationServicesMustResideInApplicationPackage() {
        classes()
                .that().resideInAPackage("..analytics..")
                .and().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..analytics.application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("62. Analytics controllers must not access repositories directly")
    void analyticsControllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..analytics.api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("63. Analytics module must not depend on hardware or actuator SDKs")
    void analyticsMustNotDependOnHardware() {
        noClasses()
                .that().resideInAPackage("..analytics..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "com.pi4j..", "com.google..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("64. Analytics domain must not depend on JPA or persistence annotations")
    void analyticsDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..analytics.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("65. Analytics domain must not depend on Spring MVC or web classes")
    void analyticsDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..analytics.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("66. Prediction controllers must reside in api package")
    void predictionControllersMustResideInApiPackage() {
        classes()
                .that().resideInAPackage("..prediction..")
                .and().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..prediction.api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("67. Prediction application services must reside in application package")
    void predictionApplicationServicesMustResideInApplicationPackage() {
        classes()
                .that().resideInAPackage("..prediction..")
                .and().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..prediction.application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("68. Prediction controllers must not access repositories directly")
    void predictionControllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..prediction.api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("69. Prediction module must not depend on hardware or actuator SDKs")
    void predictionMustNotDependOnHardware() {
        noClasses()
                .that().resideInAPackage("..prediction..")
                .should().dependOnClassesThat().resideInAnyPackage("org.eclipse.paho..", "com.pi4j..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("70. Prediction domain must not depend on JPA or persistence annotations")
    void predictionDomainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..prediction.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("71. Prediction domain must not depend on Spring MVC or web classes")
    void predictionDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..prediction.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("72. Prediction domain and application contracts must not depend on Gemini SDK or vendor classes")
    void predictionMustNotDependOnGemini() {
        noClasses()
                .that().resideInAnyPackage("..prediction.domain..", "..prediction.application..")
                .should().dependOnClassesThat().resideInAnyPackage("com.google..", "org.springframework.ai..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("73. Prediction governance domain models must not depend on persistence or database classes")
    void predictionGovernanceDomainMustNotDependOnPersistence() {
        noClasses()
                .that().resideInAPackage("..prediction.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..", "org.hibernate..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("74. Prediction governance domain services must not depend on web or servlet classes")
    void predictionGovernanceDomainMustNotDependOnWeb() {
        noClasses()
                .that().resideInAPackage("..prediction.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
                .check(importedClasses);
    }
}
