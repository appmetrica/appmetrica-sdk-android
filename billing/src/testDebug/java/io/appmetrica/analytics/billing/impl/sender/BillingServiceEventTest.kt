package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

internal class BillingServiceEventTest : CommonTest() {

    @Test
    fun defaultFields() {
        val valueBytes = "billing payload".toByteArray()
        ObjectPropertyAssertions(
            BillingServiceEvent(
                valueBytes = valueBytes,
            )
        )
            .withFinalFieldOnly(false)
            .checkField("type", Constants.Events.TYPE)
            .checkField("valueBytes", valueBytes)
            .checkField("customType", 0)
            .checkField<String>("name", null)
            .checkField<String>("value", null)
            .checkField<Int>("valueProtocolVersion", null)
            .checkField("bytesTruncated", 0)
            .checkField("extras", emptyMap<String, ByteArray>())
            .checkAll()
    }
}
