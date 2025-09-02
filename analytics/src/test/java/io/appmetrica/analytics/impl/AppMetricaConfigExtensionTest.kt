package io.appmetrica.analytics.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class AppMetricaConfigExtensionTest : CommonTest() {

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
