package io.appmetrica.analytics.impl

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.impl.utils.DeviceTypeProvider
import io.appmetrica.gradle.androidtestutils.rules.ContextRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
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
    private val windowMetrics: WindowMetrics = mock()
    private val resources: Resources = mock()

    private lateinit var displayMetrics: DisplayMetrics

    @get:Rule
    val deviceTypeProviderRule = staticRule<DeviceTypeProvider> {
        on { DeviceTypeProvider.getDeviceType(any(), any()) } doReturn DeviceTypeValues.PHONE
    }

    @Before
    fun setUp() {
        displayMetrics = DisplayMetrics()
        whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager)
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(context.resources).thenReturn(resources)
        whenever(resources.displayMetrics).thenReturn(displayMetrics)
    }

    private fun stubMaximumWindowMetrics(width: Int, height: Int) {
        whenever(windowManager.maximumWindowMetrics).thenReturn(windowMetrics)
        whenever(windowMetrics.bounds).thenReturn(Rect(0, 0, width, height))
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun maximumWindowMetricsWithActivityContext() {
        val activity = mock<Activity>()
        val width = 777
        val height = 666
        whenever(activity.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager)
        stubMaximumWindowMetrics(width, height)

        val screenInfo = screenInfoExtractor.extractScreenInfo(activity)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(activity).getSystemService(Context.WINDOW_SERVICE)
        verify(windowManager).maximumWindowMetrics
        verify(activity, never()).createWindowContext(any<Display>(), any(), any())
        verify(activity, never()).createWindowContext(any<Int>(), any())
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun maximumWindowMetricsWhenCreateWindowContextThrows() {
        val width = 777
        val height = 666
        whenever(context.display).thenReturn(display)
        whenever(
            context.createWindowContext(
                eq(display),
                eq(WindowManager.LayoutParams.TYPE_APPLICATION),
                isNull()
            )
        ).thenThrow(RuntimeException())
        stubMaximumWindowMetrics(width, height)

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(windowManager).maximumWindowMetrics
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun maximumWindowMetricsFromWindowContextWhenWindowManagerFromContextIsNull() {
        val windowContext = mock<Context>()
        val width = 999
        val height = 888
        whenever(context.display).thenReturn(display)
        whenever(
            context.createWindowContext(
                eq(display),
                eq(WindowManager.LayoutParams.TYPE_APPLICATION),
                isNull()
            )
        ).thenReturn(windowContext)
        whenever(windowContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null)
        stubMaximumWindowMetrics(width, height)

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(windowManager).maximumWindowMetrics
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun maximumWindowMetricsFromWindowContext() {
        val windowContext = mock<Context>()
        val windowContextWindowManager = mock<WindowManager>()
        val width = 999
        val height = 888
        whenever(context.display).thenReturn(display)
        whenever(
            context.createWindowContext(
                eq(display),
                eq(WindowManager.LayoutParams.TYPE_APPLICATION),
                isNull()
            )
        ).thenReturn(windowContext)
        whenever(windowContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowContextWindowManager)
        whenever(windowContextWindowManager.maximumWindowMetrics).thenReturn(windowMetrics)
        whenever(windowMetrics.bounds).thenReturn(Rect(0, 0, width, height))

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(windowContextWindowManager).maximumWindowMetrics
        verify(windowManager, never()).maximumWindowMetrics
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun maximumWindowMetricsThrows() {
        whenever(windowManager.maximumWindowMetrics).thenThrow(RuntimeException())

        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun nullWindowManagerFromS() {
        whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null)

        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun densityGetDisplayMetricsSuccessfulFromS() {
        val dpi = DisplayMetrics.DENSITY_300
        val scaleFactor = 5.7f
        displayMetrics.densityDpi = dpi
        displayMetrics.density = scaleFactor
        stubMaximumWindowMetrics(100, 200)

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.dpi).isEqualTo(dpi)
        assertThat(screenInfo.scaleFactor).isEqualTo(scaleFactor)
    }

    @Config(sdk = [Build.VERSION_CODES.R])
    @Test
    fun maximumWindowMetricsFromWindowContextApi30() {
        val windowContext = mock<Context>()
        val windowContextWindowManager = mock<WindowManager>()
        val width = 999
        val height = 888
        whenever(
            context.createWindowContext(
                eq(WindowManager.LayoutParams.TYPE_APPLICATION),
                isNull()
            )
        ).thenReturn(windowContext)
        whenever(windowContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowContextWindowManager)
        whenever(windowContextWindowManager.maximumWindowMetrics).thenReturn(windowMetrics)
        whenever(windowMetrics.bounds).thenReturn(Rect(0, 0, width, height))

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(context).createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION, null)
        verify(windowContextWindowManager).maximumWindowMetrics
        verify(windowManager, never()).maximumWindowMetrics
    }

    @Config(sdk = [Build.VERSION_CODES.R])
    @Test
    fun maximumWindowMetricsWhenCreateWindowContextThrowsApi30() {
        val width = 777
        val height = 666
        whenever(
            context.createWindowContext(
                eq(WindowManager.LayoutParams.TYPE_APPLICATION),
                isNull()
            )
        ).thenThrow(RuntimeException())
        stubMaximumWindowMetrics(width, height)

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
        verify(windowManager).maximumWindowMetrics
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun nullWindowManagerBeforeR() {
        whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null)

        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun nullDisplayBeforeR() {
        whenever(windowManager.defaultDisplay).thenReturn(null)

        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsThrowsBeforeR() {
        whenever(display.getRealMetrics(any())).thenThrow(RuntimeException())

        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun normalizesWidthAndHeightBeforeR() {
        whenever(display.getRealMetrics(any())).thenAnswer {
            val metrics = it.arguments[0] as DisplayMetrics
            metrics.widthPixels = 666
            metrics.heightPixels = 777
            null
        }

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(777)
        assertThat(screenInfo.height).isEqualTo(666)
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun getRealMetricsSuccessfulBeforeR() {
        val width = 777
        val height = 666
        whenever(display.getRealMetrics(any())).thenAnswer {
            val metrics = it.arguments[0] as DisplayMetrics
            metrics.widthPixels = width
            metrics.heightPixels = height
            null
        }

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        assertThat(screenInfo!!.width).isEqualTo(width)
        assertThat(screenInfo.height).isEqualTo(height)
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun densityGetDisplayMetricsThrowsBeforeR() {
        whenever(resources.displayMetrics).thenThrow(NullPointerException())
        whenever(display.getRealMetrics(any())).thenAnswer {
            val metrics = it.arguments[0] as DisplayMetrics
            metrics.widthPixels = 100
            metrics.heightPixels = 200
            null
        }

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.width).isEqualTo(200)
        softly.assertThat(screenInfo.height).isEqualTo(100)
        softly.assertThat(screenInfo.dpi).isZero
        softly.assertThat(screenInfo.scaleFactor).isZero
        softly.assertAll()
    }

    @Config(sdk = [Build.VERSION_CODES.Q])
    @Test
    fun densityGetDisplayMetricsSuccessfulBeforeR() {
        val dpi = DisplayMetrics.DENSITY_300
        val scaleFactor = 5.7f
        displayMetrics.densityDpi = dpi
        displayMetrics.density = scaleFactor
        whenever(display.getRealMetrics(any())).thenAnswer {
            val metrics = it.arguments[0] as DisplayMetrics
            metrics.widthPixels = 100
            metrics.heightPixels = 200
            null
        }

        val screenInfo = screenInfoExtractor.extractScreenInfo(context)

        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.dpi).isEqualTo(dpi)
        softly.assertThat(screenInfo.scaleFactor).isEqualTo(scaleFactor)
        softly.assertAll()
    }
}
