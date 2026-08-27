package io.appmetrica.analytics

import io.appmetrica.analytics.impl.FeaturesResult
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

class FeaturesResultTest : CommonTest() {

    @Test
    fun constructor() {
        val sslPinning = false
        val result = FeaturesResult(sslPinning)
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkField("libSslEnabled", "getLibSslEnabled", sslPinning)
            .checkAll()
    }
}
