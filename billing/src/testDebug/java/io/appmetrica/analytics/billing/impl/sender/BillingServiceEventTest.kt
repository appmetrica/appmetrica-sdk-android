package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test
import java.nio.charset.StandardCharsets

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
            .checkField("value", "getValue", String(valueBytes, StandardCharsets.UTF_8))
            .checkField<Int>("valueProtocolVersion", null)
            .checkField("bytesTruncated", 0)
            .checkField("extras", emptyMap<String, ByteArray>())
            .checkAll()
    }
}
