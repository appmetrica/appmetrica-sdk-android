package io.appmetrica.analytics.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModulesIsolationTest : CommonTest() {

    @Test
    fun productFlowClassesAreNotUsedInAnalyticsModule() {
        val analyticsClassesUrl = AppMetrica::class.java.protectionDomain.codeSource.location
        val classes = ClassFileImporter().importUrl(analyticsClassesUrl)

        assertThat(classes)
            .describedAs("ArchUnit did not import any class — check the AppMetrica code source")
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
