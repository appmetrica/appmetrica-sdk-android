package io.appmetrica.analytics

import io.appmetrica.analytics.AdsIdentifiersResult.AdvId
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdsIdentifiersResultAdvIdTest : CommonTest() {

    @Test
    fun constructor() {
        val identifier = "some identifier"
        val details = AdsIdentifiersResult.Details.NO_STARTUP
        val error = "some error"
        val advId = AdvId(identifier, details, error)
        ObjectPropertyAssertions(advId)
            .checkField("advId", identifier)
            .checkField("details", details)
            .checkField("errorExplanation", error)
            .checkAll()
    }
}
