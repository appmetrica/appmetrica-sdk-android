package io.appmetrica.analytics.impl.network

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
internal class ConnectionBasedExecutionPolicyTest(
    private val networkType: NetworkType,
    private val expectedValue: Boolean
) : CommonTest() {

    companion object {

        @Parameterized.Parameters(name = "Do not should start task in network {0}? - {1}")
        @JvmStatic
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(NetworkType.OFFLINE, false),
                arrayOf(NetworkType.UNDEFINED, true),
                arrayOf(NetworkType.CELL, true),
                arrayOf(NetworkType.WIFI, true),
            )
        }
    }

    private val context: Context = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val policy by setUp { ConnectionBasedExecutionPolicy(context) }

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().activeNetworkTypeProvider.getNetworkType(context))
            .doReturn(networkType)
    }

    @Test
    fun canBeExecuted() {
        Assertions.assertThat(policy.canBeExecuted()).isEqualTo(expectedValue)
    }
}
