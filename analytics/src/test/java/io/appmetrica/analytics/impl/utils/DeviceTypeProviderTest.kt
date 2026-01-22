package io.appmetrica.analytics.impl.utils

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.util.DisplayMetrics
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DeviceTypeProviderTest : CommonTest() {

    private val resources: Resources = mock()
    private val context: Context = mock {
        on { resources } doReturn resources
    }

    @get:Rule
    val systemServiceUtilsRule = staticRule<SystemServiceUtils>()

    @Test
    fun deviceTypeZeroDensity() {
        val displayMetrics = DisplayMetrics()
        displayMetrics.density = 0f
        whenever(resources.displayMetrics).thenReturn(displayMetrics)

        assertThat(DeviceTypeProvider.getDeviceType(context, Point(0, 0))).isEqualTo(DeviceTypeValues.PHONE)
    }

    @Test
    fun deviceTypeGetDisplayMetricsThrows() {
        val resources = Mockito.mock(Resources::class.java)
        whenever(resources.displayMetrics).thenThrow(RuntimeException())

        assertThat(DeviceTypeProvider.getDeviceType(context, Point(0, 0))).isEqualTo(DeviceTypeValues.PHONE)
    }

    @Test
    fun getDeviceTypeForTv() {
        val displayMetrics = DisplayMetrics()
        displayMetrics.density = 1f
        whenever(resources.displayMetrics).thenReturn(displayMetrics)

        val uiModeManager: UiModeManager = mock()
        whenever(context.getSystemService(Context.UI_MODE_SERVICE)).thenReturn(uiModeManager)

        stubbing(systemServiceUtilsRule.staticMock) {
            on {
                SystemServiceUtils.accessSystemServiceSafelyOrDefault<UiModeManager, Int?>(
                    eq(uiModeManager),
                    eq("getting current mode type"),
                    eq("UiModeManager"),
                    eq(null),
                    any()
                )
            } doReturn Configuration.UI_MODE_TYPE_TELEVISION
        }

        val deviceType = DeviceTypeProvider.getDeviceType(context, Point(0, 0))
        assertThat(deviceType).isEqualTo(DeviceTypeValues.TV)
    }

    @Test
    fun deviceTypeForPhone() {
        assertDeviceTypeForPhoneParams(1080, 1920, 2f, DeviceTypeValues.PHONE) // Hightscreen Boost3
        assertDeviceTypeForPhoneParams(720, 1280, 2f, DeviceTypeValues.PHONE) // Huawei ASCEND P6 U06
        assertDeviceTypeForPhoneParams(1440, 2560, 4f, DeviceTypeValues.PHONE) // LG G3 (LG-D855)
        assertDeviceTypeForPhoneParams(800, 1280, 1f, DeviceTypeValues.TABLET) // Galaxy Tab 2 (GT-P5110)
        assertDeviceTypeForPhoneParams(1600, 2560, 2f, DeviceTypeValues.TABLET) // Samsung Nexus 10
        assertDeviceTypeForPhoneParams(600, 1024, 1f, DeviceTypeValues.TABLET) // Galaxy Tab 3 (SM-T210)
        assertDeviceTypeForPhoneParams(800, 1280, 1.33f, DeviceTypeValues.TABLET) // nexus 7
    }

    private fun assertDeviceTypeForPhoneParams(width: Int, height: Int, density: Float, phone: String) {
        val displayMetrics = DisplayMetrics()
        displayMetrics.density = density
        whenever(resources.displayMetrics).thenReturn(displayMetrics)

        val deviceType = DeviceTypeProvider.getDeviceType(context, Point(width, height))

        assertThat(deviceType).isEqualTo(phone)
    }
}
