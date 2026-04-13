package io.github.anderslunddev.pedalboard;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "io.github.anderslunddev.pedalboard", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule controllers_should_not_access_persistence =
		noClasses().that().resideInAPackage("..api.controller..")
			.should().dependOnClassesThat().resideInAPackage("..model..")
			.because("Controllers must access persistence through the service layer");

	@ArchTest
	static final ArchRule persistence_should_not_access_api =
		noClasses().that().resideInAPackage("..model..")
			.should().dependOnClassesThat().resideInAPackage("..api..")
			.because("Persistence layer must not know about API controllers or DTOs");

	@ArchTest
	static final ArchRule domain_should_not_depend_on_infrastructure =
		noClasses().that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
				"..model..", "..api..", "..service..", "..security..", "..config..", "..port..")
			.because("Domain must remain independent of application and infrastructure concerns");

	@ArchTest
	static final ArchRule port_must_not_depend_on_implementation_layers =
		noClasses().that().resideInAPackage("..port..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"..model..", "..api..", "..service..", "..security..", "..config..")
			.because("Outgoing ports only declare domain types; implementations live in persistence");

	@ArchTest
	static final ArchRule services_should_not_depend_on_persistence_impl =
		noClasses().that().resideInAPackage("..service..")
			.should().dependOnClassesThat().resideInAPackage("..model..")
			.because("Services depend on port interfaces, not JPA adapters or entities");

	@ArchTest
	static final ArchRule security_should_not_depend_on_persistence_impl =
		noClasses().that().resideInAPackage("..security..")
			.should().dependOnClassesThat().resideInAPackage("..model..")
			.because("Security uses port interfaces, not persistence implementation types");

	@ArchTest
	static final ArchRule services_should_not_depend_on_api =
		noClasses().that().resideInAPackage("..service..")
			.should().dependOnClassesThat().resideInAPackage("..api..")
			.because("Services must not depend on the API/presentation layer");

	@ArchTest
	static final ArchRule layered_architecture_is_respected = layeredArchitecture()
		.consideringAllDependencies()
		.layer("API").definedBy("..api..")
		.layer("Service").definedBy("..service..")
		.layer("Port").definedBy("..port..")
		.layer("Persistence").definedBy("..model..")
		.layer("Domain").definedBy("..domain..")
		.layer("Security").definedBy("..security..")
		.layer("Config").definedBy("..config..")

		.whereLayer("API").mayNotBeAccessedByAnyLayer()
		.whereLayer("Service").mayOnlyBeAccessedByLayers("API")
		.whereLayer("Port").mayOnlyBeAccessedByLayers("Service", "Security", "Persistence")
		.whereLayer("Persistence").mayOnlyBeAccessedByLayers("Persistence")
		.whereLayer("Security").mayOnlyBeAccessedByLayers("API")
		.whereLayer("Config").mayNotBeAccessedByAnyLayer()

		.ignoreDependency(
			JavaClass.Predicates.belongToAnyOf(PedalboardBackendApplication.class),
			DescribedPredicate.alwaysTrue());
}
