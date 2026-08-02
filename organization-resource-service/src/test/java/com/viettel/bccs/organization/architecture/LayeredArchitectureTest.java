package com.viettel.bccs.organization.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.viettel.bccs.test.architecture.BccsArchitectureRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.viettel.bccs.organization",
        importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule controllers_do_not_depend_on_technical_layers = noClasses()
            .that().resideInAnyPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..repository..", "..entity..", "..client..", "..cache..", "..event..")
            .because("controllers delegate transport requests to their feature service")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_are_rest_controllers = classes()
            .that().resideInAnyPackage("..controller..")
            .should().beAnnotatedWith(RestController.class)
            .andShould().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_do_not_depend_on_controllers_or_entities = noClasses()
            .that().resideInAnyPackage("..service..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..entity..")
            .because("services orchestrate models and layered collaborators")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule services_are_spring_services = classes()
            .that().resideInAnyPackage("..service..")
            .should().beAnnotatedWith(Service.class)
            .andShould().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule repositories_do_not_depend_on_upper_or_external_layers = noClasses()
            .that().resideInAnyPackage("..repository..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..controller..", "..service..", "..client..", "..cache..", "..event..")
            .because("repositories only implement persistence access")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entities_are_persistence_leaves = noClasses()
            .that().resideInAnyPackage("..entity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..controller..", "..service..", "..repository..", "..client..",
                    "..cache..", "..event..", "..mapper..")
            .because("persistence entities must not know application layers")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule common_does_not_depend_on_features = noClasses()
            .that().resideInAnyPackage("..common..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..sampleorder..", "..sampleredis..", "..outboundsample..",
                    "..optionset..", "..sampleerror..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule option_set_demo_is_independent = featureDoesNotDependOnOthers(
            "optionset", "sampleorder", "sampleredis", "outboundsample", "sampleerror");

    @ArchTest
    static final ArchRule transactions_are_declared_in_services = methods()
            .that().areAnnotatedWith(Transactional.class)
            .should().beDeclaredInClassesThat().resideInAnyPackage("..service..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule no_webflux = BccsArchitectureRules.NO_WEBFLUX_ALLOWED;

    @ArchTest
    static final ArchRule no_direct_native_redis = BccsArchitectureRules.NO_DIRECT_REDIS_CLIENT_USAGE;

    @ArchTest
    static final ArchRule no_direct_error_response_creation =
            BccsArchitectureRules.NO_DIRECT_ERROR_RESPONSE_CREATION;

    private static ArchRule featureDoesNotDependOnOthers(String feature, String... otherFeatures) {
        String[] forbiddenPackages = new String[otherFeatures.length];
        for (int i = 0; i < otherFeatures.length; i++) {
            forbiddenPackages[i] = ".." + otherFeatures[i] + "..";
        }
        return noClasses()
                .that().resideInAnyPackage(".." + feature + "..")
                .should().dependOnClassesThat().resideInAnyPackage(forbiddenPackages)
                .because("cross-feature dependencies require an explicit architecture allow-list")
                .allowEmptyShould(true);
    }
}