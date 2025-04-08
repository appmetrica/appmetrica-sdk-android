package io.appmetrica.analytics

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
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
