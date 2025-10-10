package io.appmetrica.analytics.idsync.impl.precondition

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.ActiveNetworkTypeProvider
import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
class CellNetworkPreconditionVerifierTest(
    private val networkType: NetworkType?,
    private val expectedValue: Boolean
) : CommonTest() {

    companion object {
        @Parameterized.Parameters(name = "Network type {0} - {1}")
        @JvmStatic
        fun data() = listOf<Array<Any?>>(
            arrayOf(NetworkType.CELL, true),
            arrayOf(NetworkType.WIFI, false),
            arrayOf(NetworkType.ETHERNET, false),
            arrayOf(NetworkType.VPN, false),
            arrayOf(NetworkType.LOWPAN, false),
            arrayOf(NetworkType.WIFI_AWARE, false),
            arrayOf(NetworkType.MOBILE_DUN, false),
            arrayOf(NetworkType.MOBILE_HIPRI, false),
            arrayOf(NetworkType.BLUETOOTH, false),
            arrayOf(NetworkType.MOBILE_MMS, false),
            arrayOf(NetworkType.MOBILE_SUPL, false),
            arrayOf(NetworkType.WIMAX, false),
            arrayOf(NetworkType.OFFLINE, false),
            arrayOf(NetworkType.UNDEFINED, false),
            arrayOf(null, false)
        )
    }

    private val context: Context = mock()
    private val activeNetworkTypeProvider: ActiveNetworkTypeProvider = mock()

    private val serviceContext: ServiceContext = mock {
        on { context }.thenReturn(context)
        on { activeNetworkTypeProvider }.thenReturn(activeNetworkTypeProvider)
    }

    private val verifier by setUp { CellNetworkPreconditionVerifier(serviceContext) }

    @Test
    fun matchPrecondition() {
        whenever(activeNetworkTypeProvider.getNetworkType(context)).thenReturn(networkType)
        assertThat(verifier.matchPrecondition()).isEqualTo(expectedValue)
    }
}
