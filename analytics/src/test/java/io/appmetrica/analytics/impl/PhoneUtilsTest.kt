@file:Suppress("DEPRECATION")

package io.appmetrica.analytics.impl

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Locale

internal class PhoneUtilsTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    @get:Rule
    val androidUtilsRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.M) } doReturn true
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.O) } doReturn true
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.O_MR1) } doReturn true
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.P) } doReturn true
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q) } doReturn true
    }
    private val connectivityManager: ConnectivityManager = mock()

    @Before
    fun setUp() {
        whenever(context.getSystemService(any<String>())).thenReturn(connectivityManager)
    }

    @Test
    fun normalizedLocaleWithScriptAndCountry() {
        val locale = Locale.Builder().setLanguage("en").setRegion("UK").setScript("Latn").build()
        assertNormalizedLocale(locale, "en-Latn_UK")
    }

    @Test
    fun normalizedLocaleWithScriptWithoutCountry() {
        val locale = Locale.Builder().setLanguage("az").setScript("Arab").build()
        assertNormalizedLocale(locale, "az-Arab")
    }

    @Test
    fun normalizedLocaleWithoutScript() {
        assertNormalizedLocale(Locale("ru", "BE"), "ru_BE")
        assertNormalizedLocale(Locale("de", "AT"), "de_AT")
        assertNormalizedLocale(Locale.FRANCE, "fr_FR")
        assertNormalizedLocale(Locale.CANADA, "en_CA")
    }

    @Test
    fun getConnectionTypeNullNetworkInfo() {
        whenever(connectivityManager.activeNetworkInfo).thenReturn(null)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.OFFLINE)
    }

    @Test
    fun activeNetworkIsNull() {
        val networkInfo: NetworkInfo = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(connectivityManager.activeNetwork).thenReturn(null)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.OFFLINE)
    }

    @Test
    fun notConnected() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)).thenReturn(false)
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val networkInfo: NetworkInfo = mock()
        val network: Network = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(false)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.OFFLINE)
    }

    @Test
    fun networkCapabilitiesNull() {
        val networkInfo: NetworkInfo = mock()
        val network: Network = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(true)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.UNDEFINED)
    }

    @Test
    fun lowPan() {
        val networkInfo: NetworkInfo = mock()
        val network: Network = mock()
        val networkCapabilities: NetworkCapabilities = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(true)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)).thenReturn(true)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.LOWPAN)
    }

    @Test
    fun wifiAware() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)).thenReturn(false)
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)).thenReturn(false)
        val networkInfo: NetworkInfo = mock()
        val network: Network = mock()
        val networkCapabilities: NetworkCapabilities = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(true)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)).thenReturn(true)
        assertThat(PhoneUtils.getConnectionType(context)).isEqualTo(NetworkType.WIFI_AWARE)
    }

    private fun assertNormalizedLocale(locale: Locale, expected: String) {
        assertThat(PhoneUtils.normalizedLocale(locale)).isEqualTo(expected)
    }
}
