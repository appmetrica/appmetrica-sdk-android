package io.appmetrica.analytics

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

class AppMetricaLibraryAdapterConfigTest : CommonTest() {

    @Test
    fun defaultConfig() {
        ObjectPropertyAssertions(AppMetricaLibraryAdapterConfig.newConfigBuilder().build())
            .checkFieldsAreNull("advIdentifiersTracking", "customHosts")
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
            .checkFieldIsNull("customHosts")
            .checkAll()
    }

    @Test
    fun filledConfigWithCustomHosts() {
        val customHosts = listOf("host1", "host2")
        val config = AppMetricaLibraryAdapterConfig.newConfigBuilder()
            .withCustomHosts(customHosts)
            .build()
        ObjectPropertyAssertions(config)
            .checkFieldIsNull("advIdentifiersTracking")
            .checkField("customHosts", customHosts)
            .checkAll()
    }
}
