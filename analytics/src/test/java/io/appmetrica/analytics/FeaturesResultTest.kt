package io.appmetrica.analytics

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.FeaturesResult
import io.appmetrica.analytics.testutils.CommonTest
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
