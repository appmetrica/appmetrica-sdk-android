package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ReporterRestrictionBasedPolicyTest : CommonTest() {

    private val restrictionController = mock<DataSendingRestrictionController>()

    private val policy by setUp { ReporterRestrictionBasedPolicy(restrictionController) }

    @Test
    fun description() {
        assertThat(policy.description()).isEqualTo("data restriction based")
    }

    @Test
    fun `canBeExecuted if restricted for sdk`() {
        whenever(restrictionController.isRestrictedForSdk).doReturn(true)
        assertThat(policy.canBeExecuted()).isFalse()
    }

    @Test
    fun `canBeExecuted if not restricted`() {
        whenever(restrictionController.isRestrictedForSdk).doReturn(false)
        assertThat(policy.canBeExecuted()).isTrue()
    }
}
