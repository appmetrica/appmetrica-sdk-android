package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenInfoHolderTest : CommonTest() {

    private lateinit var screenInfoHolder: ScreenInfoHolder
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        screenInfoHolder = ScreenInfoHolder()
    }

    @Test
    fun initialScreenInfo() {
        ObjectPropertyAssertions(screenInfoHolder.screenInfo)
            .checkField("width", "getWidth",0)
            .checkField("height", "getHeight",0)
            .checkField("dpi", "getDpi", 0)
            .checkFloatField("scaleFactor", "getScaleFactor", 0f, 0.0000001f)
            .checkField("deviceType", DeviceTypeValues.PHONE)
            .checkAll()
    }

    @Test
    fun updateToNull() {
        val initialScreenInfo = screenInfoHolder.screenInfo
        screenInfoHolder.maybeUpdateInfo(null)
        assertThat(screenInfoHolder.screenInfo).isSameAs(initialScreenInfo)
    }

    @Test
    fun updateToNonNull() {
        val newDensity = 66.6f
        val newDensityDpi = 999
        val newDeviceType = DeviceTypeValues.TABLET
        val newWidth = 343434
        val newHeight = 2323
        val newScreenInfo = ScreenInfo(newWidth, newHeight, newDensityDpi, newDensity, newDeviceType)
        screenInfoHolder.maybeUpdateInfo(newScreenInfo)
        assertThat(screenInfoHolder.screenInfo).isEqualTo(newScreenInfo)
    }
}
