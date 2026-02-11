package io.appmetrica.analytics

import io.appmetrica.analytics.AdvIdentifiersResult.AdvId
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class AdvIdentifiersResultAdvIdTest : CommonTest() {

    @Test
    fun constructor() {
        val identifier = "some identifier"
        val details = AdvIdentifiersResult.Details.INTERNAL_ERROR
        val error = "some error"
        val advId = AdvId(identifier, details, error)
        ObjectPropertyAssertions(advId)
            .checkField("advId", identifier)
            .checkField("details", details)
            .checkField("errorExplanation", error)
            .checkAll()
    }
}
