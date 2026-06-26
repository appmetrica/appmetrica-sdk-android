package io.appmetrica.analytics.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModulesIsolationTest : CommonTest() {

    @Test
    fun productFlowClassesAreNotUsedInAnalyticsModule() {
        val classes = ClassFileImporter()
            .withImportOption { location ->
                val path = location.asURI().toString()
                // only main classes of the analytics module itself; AGP packs
                // them into runtime_library_classes_jar for unit-test runtime.
                "build/intermediates/runtime_library_classes_jar/" in path
            }
            .importPackages("io.appmetrica.analytics")

        assertThat(classes)
            .describedAs("ArchUnit did not import any class — check the location filter")
            .isNotEmpty

        noClasses()
            .that().resideOutsideOfPackage("..productflow..")
            .should().dependOnClassesThat()
            .resideInAPackage("..productflow..")
            .because(
                "product-flow must remain safely disableable in prod, so analytics " +
                    "must not reference its classes directly"
            )
            .check(classes)
    }
}
