package io.appmetrica.analytics

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

class AppMetricaLibraryAdapterConfigTest : CommonTest() {

    @Test
    fun defaultConfig() {
        ObjectPropertyAssertions(AppMetricaLibraryAdapterConfig.newConfigBuilder().build())
            .checkFieldsAreNull("advIdentifiersTracking")
            .checkAll()
    }

    @Test
    fun filledConfig() {
        val advIdentifiersTracking = true
        val config = AppMetricaLibraryAdapterConfig.newConfigBuilder()
            .withAdvIdentifiersTracking(advIdentifiersTracking)
            .build()
        ObjectPropertyAssertions(config)
            .checkField("advIdentifiersTracking", advIdentifiersTracking)
            .checkAll()
    }
}
