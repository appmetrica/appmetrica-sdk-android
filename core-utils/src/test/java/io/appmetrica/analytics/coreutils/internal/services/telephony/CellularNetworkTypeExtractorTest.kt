package io.appmetrica.analytics.coreutils.internal.services.telephony

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
internal class CellularNetworkTypeExtractorTest : CommonTest() {

    private val actualNetworkType = TelephonyManager.NETWORK_TYPE_LTE
    private val legacyNetworkType = TelephonyManager.NETWORK_TYPE_HSPA
    private val actualNetworkTypeString = "Actual network type"
    private val legacyNetworkTypeString = "Legacy network type"

    private val telephonyManager: TelephonyManager = mock {
        on { networkType } doReturn legacyNetworkType
        on { dataNetworkType } doReturn actualNetworkType
    }

    @get:Rule
    val converterMockedStaticRule = staticRule<CellularNetworkTypeConverter> {
        on { CellularNetworkTypeConverter.convert(actualNetworkType) } doReturn actualNetworkTypeString
        on { CellularNetworkTypeConverter.convert(legacyNetworkType) } doReturn legacyNetworkTypeString
    }

    private val context: Context = mock()

    @get:Rule
    val systemServiceUtilsMockedStaticRule = staticRule<SystemServiceUtils> {
        on {
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.TELEPHONY_SERVICE),
                any(),
                any(),
                any<FunctionWithThrowable<TelephonyManager, Int?>>()
            )
        } doAnswer {
            (it.arguments[4] as FunctionWithThrowable<TelephonyManager, Int?>).apply(telephonyManager)
        }
    }

    private val networkTypeExtractor: CellularNetworkTypeExtractor by setUp { CellularNetworkTypeExtractor(context) }

    @Config(sdk = [Build.VERSION_CODES.M])
    @Test
    fun `getNetworkType pre N`() {
        assertThat(networkTypeExtractor.getNetworkType()).isEqualTo(legacyNetworkTypeString)
    }

    @Config(sdk = [Build.VERSION_CODES.M])
    @Test
    fun `getNetworkType pre N if returns null`() {
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.TELEPHONY_SERVICE),
                any(),
                any(),
                any<FunctionWithThrowable<TelephonyManager, Int?>>()
            )
        ).thenReturn(null)
        assertThat(networkTypeExtractor.getNetworkType()).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `getNetworkType for N`() {
        assertThat(networkTypeExtractor.getNetworkType()).isEqualTo(actualNetworkTypeString)
    }

    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `getNetworkType for N if returns null`() {
        whenever(
            SystemServiceUtils.accessSystemServiceByNameSafely(
                eq(context),
                eq(Context.TELEPHONY_SERVICE),
                any(),
                any(),
                any<FunctionWithThrowable<TelephonyManager, Int?>>()
            )
        ).thenReturn(null)
        assertThat(networkTypeExtractor.getNetworkType()).isNull()
    }
}
