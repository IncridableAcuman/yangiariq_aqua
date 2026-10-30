package com.aquaai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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
    @DisplayName("Controllers must reside in api packages")
    void controllersShouldResideInApiPackage() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..api..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Services must reside in application or infrastructure packages")
    void servicesShouldResideInApplicationOrInfrastructurePackage() {
        classes()
                .that().areAnnotatedWith(Service.class)
                .should().resideInAnyPackage("..application..", "..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Repositories must not be directly accessed from Controllers")
    void controllersMustNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controllers should have 'Controller' suffix")
    void controllersShouldHaveSuffix() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .check(importedClasses);
    }
}
