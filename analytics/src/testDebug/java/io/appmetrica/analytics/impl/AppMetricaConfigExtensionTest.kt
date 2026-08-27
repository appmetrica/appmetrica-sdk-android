package io.appmetrica.analytics.impl

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

internal class AppMetricaConfigExtensionTest : CommonTest() {

    @Test
    fun config() {
        val autoCollectedDataSubscribers = listOf("first", "second")
        val needClearEnvironment = true

        ObjectPropertyAssertions(AppMetricaConfigExtension(autoCollectedDataSubscribers, needClearEnvironment))
            .checkField("autoCollectedDataSubscribers", autoCollectedDataSubscribers)
            .checkField("needClearEnvironment", needClearEnvironment)
            .checkAll()
    }
}
