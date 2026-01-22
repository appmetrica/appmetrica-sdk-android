package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class NativeMainReporterEventSendingPolicyTest : CommonTest() {

    private val policy: NativeMainReporterEventSendingPolicy by setUp { NativeMainReporterEventSendingPolicy() }

    @Test
    fun condition() {
        assertThat(policy.condition.isConditionMet).isTrue()
    }
}
