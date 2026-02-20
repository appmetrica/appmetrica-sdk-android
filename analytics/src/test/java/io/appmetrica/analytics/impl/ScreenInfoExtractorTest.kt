package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.impl.utils.DeviceTypeProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
internal class ScreenInfoExtractorTest : CommonTest() {

    private val screenInfoExtractor = ScreenInfoExtractor()

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val windowManager: WindowManager = mock()
    private val display: Display = mock()
    private val resources: Resources = mock()

    private lateinit var displayMetrics: DisplayMetrics

    @get:Rule
    val deviceTypeProviderRule = staticRule<DeviceTypeProvider> {
        on { DeviceTypeProvider.getDeviceType(any(), any()) } doReturn DeviceTypeValues.PHONE
    }

    @get:Rule
    val androidUtilsRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.R) } doReturn true
    }

    @Before
    fun setUp() {
        displayMetrics = DisplayMetrics()
        whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager)
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(context.resources).thenReturn(resources)
        whenever(resources.displayMetrics).thenReturn(displayMetrics)
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun nullWindowManagerBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null)
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Test
    fun nonNullDisplay() {
        whenever(context.display).thenReturn(display)

        val width = 777
        val height = 666
        whenever(display.getRealMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = width
            displayMetrics.heightPixels = height
            null
        }
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        softly.assertAll()

        verify(context, never()).getSystemService(Context.WINDOW_SERVICE)
    }

    @Test
    fun nonNullDisplayThrows() {
        whenever(context.display).thenReturn(display)

        whenever(display.getRealMetrics(any())).thenThrow(RuntimeException())
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()

        verify(context, never()).getSystemService(Context.WINDOW_SERVICE)
    }

    @Test
    fun nullDisplay() {
        whenever(context.display).thenReturn(null)

        val width = 777
        val height = 666
        whenever(display.getRealMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = width
            displayMetrics.heightPixels = height
            null
        }

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.width).isEqualTo(width)
        softly.assertThat(screenInfo.height).isEqualTo(height)
        softly.assertAll()
        verify(context).getSystemService(Context.WINDOW_SERVICE)
        verify(windowManager).defaultDisplay
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun nullDisplayBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        whenever(windowManager.defaultDisplay).thenReturn(null)
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsThrowsBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        whenever(display.getRealMetrics(any())).thenThrow(RuntimeException())
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsSuccessfulWithIsGreaterThanHeightBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        val width = 777
        val height = 666
        whenever(display.getRealMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = width
            displayMetrics.heightPixels = height
            null
        }
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.width).isEqualTo(width)
        softly.assertThat(screenInfo.height).isEqualTo(height)
        softly.assertAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsSuccessfulWithIsLessThanHeightBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        val width = 666
        val height = 777
        whenever(display.getRealMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = width
            displayMetrics.heightPixels = height
            null
        }
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.width).isEqualTo(height)
        softly.assertThat(screenInfo.height).isEqualTo(width)
        softly.assertAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsSuccessfulWithIsEqualToHeightBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        val width = 666
        whenever(display.getRealMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = width
            displayMetrics.heightPixels = width
            null
        }
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.width).isEqualTo(width)
        softly.assertThat(screenInfo.height).isEqualTo(width)
        softly.assertAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun densityGetDisplayMetricsThrowsBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        whenever(resources.displayMetrics).thenThrow(NullPointerException())
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.dpi).isZero
        softly.assertThat(screenInfo.scaleFactor).isZero
        softly.assertAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun densityGetDisplayMetricsSuccessfulBeforeR() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)).thenReturn(false)
        val dpi = DisplayMetrics.DENSITY_300
        val scaleFactor = 5.7f
        displayMetrics.densityDpi = dpi
        displayMetrics.density = scaleFactor
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.dpi).isEqualTo(dpi)
        softly.assertThat(screenInfo.scaleFactor).isEqualTo(scaleFactor)
        softly.assertAll()
    }
}
