package io.appmetrica.analytics.impl

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ScreenInfoExtractorTest : CommonTest() {

    private val screenInfoExtractor = ScreenInfoExtractor()
    @Mock
    private lateinit var windowManager: WindowManager
    @Mock
    private lateinit var display: Display
    private lateinit var context: Context
    @Mock
    private lateinit var resources: Resources
    private lateinit var displayMetrics: DisplayMetrics

    @Rule
    @JvmField
    val sPhoneUtils = MockedStaticRule(PhoneUtils::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        displayMetrics = DisplayMetrics()
        `when`(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager)
        `when`(windowManager.defaultDisplay).thenReturn(display)
        `when`(context.resources).thenReturn(resources)
        `when`(resources.displayMetrics).thenReturn(displayMetrics)
        whenever(PhoneUtils.getDeviceType(eq(context), any())).thenReturn(DeviceTypeValues.PHONE)
    }

    @Test
    fun nullWindowManager() {
        `when`(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null)
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun nonNullDisplayForR() {
        `when`(context.display).thenReturn(display)

        val width = 777
        val height = 666
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenAnswer {
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
    @Config(sdk = [Build.VERSION_CODES.R])
    fun nonNullDisplayForRThrows() {
        `when`(context.display).thenReturn(display)

        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenThrow(RuntimeException())
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()

        verify(context, never()).getSystemService(Context.WINDOW_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun nullDisplayForR() {
        `when`(context.display).thenReturn(null)

        val width = 777
        val height = 666
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenAnswer {
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

    @Test
    fun nullDisplay() {
        `when`(windowManager.defaultDisplay).thenReturn(null)
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Test
    fun getRealMetricsThrows() {
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenThrow(RuntimeException())
        assertThat(screenInfoExtractor.extractScreenInfo(context)).isNull()
    }

    @Test
    fun getRealMetricsSuccessfulWithIsGreaterThanHeight() {
        val width = 777
        val height = 666
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenAnswer {
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

    @Test
    fun getRealMetricsSuccessfulWithIsLessThanHeight() {
        val width = 666
        val height = 777
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenAnswer {
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

    @Test
    fun getRealMetricsSuccessfulWithIsEqualToHeight() {
        val width = 666
        `when`(display.getRealMetrics(any(DisplayMetrics::class.java))).thenAnswer {
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

    @Test
    fun densityGetDisplayMetricsThrows() {
        `when`(resources.displayMetrics).thenThrow(NullPointerException())
        val screenInfo = screenInfoExtractor.extractScreenInfo(context)
        val softly = SoftAssertions()
        softly.assertThat(screenInfo!!.dpi).isZero
        softly.assertThat(screenInfo.scaleFactor).isZero
        softly.assertAll()
    }

    @Test
    fun densityGetDisplayMetricsSuccessful() {
        val dpi = 555
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
