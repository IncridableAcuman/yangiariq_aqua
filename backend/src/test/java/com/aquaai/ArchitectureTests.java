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
}
