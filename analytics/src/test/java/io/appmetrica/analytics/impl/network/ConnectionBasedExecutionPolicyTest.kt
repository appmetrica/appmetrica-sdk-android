package io.appmetrica.analytics.impl.network

import android.content.Context
import io.appmetrica.analytics.impl.PhoneUtils
import io.appmetrica.analytics.impl.utils.ConnectionTypeProviderImpl
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ConnectionBasedExecutionPolicyTest(
    private val networkType: PhoneUtils.NetworkType,
    private val expectedValue: Boolean
) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "Do not should start task in network {0}? - {1}")
        @JvmStatic
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(PhoneUtils.NetworkType.OFFLINE, false),
                arrayOf(PhoneUtils.NetworkType.UNDEFINED, true),
                arrayOf(PhoneUtils.NetworkType.CELL, true),
                arrayOf(PhoneUtils.NetworkType.WIFI, true),
            )
        }
    }

    private val context: Context = mock()

    @get:Rule
    val connectionTypeProviderImplRule = constructionRule<ConnectionTypeProviderImpl> {
        on { getConnectionType(context) } doReturn networkType
    }

    private val policy by setUp { ConnectionBasedExecutionPolicy(context) }

    @Test
    fun canBeExecuted() {
        Assertions.assertThat(policy.canBeExecuted()).isEqualTo(expectedValue)
    }
}
