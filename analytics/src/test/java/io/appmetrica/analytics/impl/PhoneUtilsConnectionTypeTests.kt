@file:Suppress("DEPRECATION")

package io.appmetrica.analytics.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class PhoneUtilsConnectionTypeTests(
    private val networkCapabilitiesType: Int,
    private val expected: NetworkType
) : CommonTest() {

    private val networkInfo: NetworkInfo = mock {
        on { isConnected } doReturn true
    }

    private val network: Network = mock()

    private val networkCapabilities: NetworkCapabilities = mock()
    private val connectivityManager: ConnectivityManager = mock {
        on { activeNetwork } doReturn network
        on { getNetworkInfo(network) } doReturn networkInfo
        on { getNetworkCapabilities(network) } doReturn networkCapabilities
    }

    private val context: Context = mock {
        on { getSystemService(any<String>()) } doReturn connectivityManager
    }

    @Before
    fun setUp() {
        whenever(networkCapabilities.hasTransport(networkCapabilitiesType)).thenReturn(true)
    }

    @Test
    fun getConnectionType() {
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "expected type {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(NetworkCapabilities.TRANSPORT_BLUETOOTH, NetworkType.BLUETOOTH),
            arrayOf(NetworkCapabilities.TRANSPORT_ETHERNET, NetworkType.ETHERNET),
            arrayOf(NetworkCapabilities.TRANSPORT_CELLULAR, NetworkType.CELL),
            arrayOf(NetworkCapabilities.TRANSPORT_VPN, NetworkType.VPN),
            arrayOf(NetworkCapabilities.TRANSPORT_WIFI, NetworkType.WIFI),
            arrayOf(NetworkCapabilities.TRANSPORT_WIFI_AWARE, NetworkType.WIFI_AWARE),
            arrayOf(NetworkCapabilities.TRANSPORT_LOWPAN, NetworkType.LOWPAN),
            arrayOf(99, NetworkType.UNDEFINED)
        )
    }
}
