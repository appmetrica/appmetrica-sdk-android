package io.appmetrica.analytics.impl.db.event

import android.location.Location
import android.location.LocationManager
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.YLocation
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DbLocationModelFactoryTest : CommonTest() {

    private val isLocationTracking = true
    private val latitude = 123.3
    private val longitude = 321.1
    private val timestamp = 123244L
    private val precision = 4324
    private val direction = 1231
    private val speed = 2131
    private val altitude = 12321
    private val provider = LocationManager.GPS_PROVIDER
    private val originalProvider = LocationManager.NETWORK_PROVIDER

    private val location: Location = mock()
    private val yLocation: YLocation = mock {
        on { latitude } doReturn latitude
        on { longitude } doReturn longitude
        on { time } doReturn timestamp
        on { accuracy } doReturn precision.toFloat()
        on { bearing } doReturn direction.toFloat()
        on { speed } doReturn speed.toFloat()
        on { altitude } doReturn altitude.toDouble()
        on { provider } doReturn provider
        on { originalProvider } doReturn originalProvider
    }

    private val reportRequestConfig: ReportRequestConfig = mock {
        on { isLocationTracking } doReturn isLocationTracking
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val yLocationRule = MockedStaticRule(YLocation::class.java)

    private val factory by setUp { DbLocationModelFactory(reportRequestConfig) }

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().locationClientApi.userLocation).thenReturn(location)
    }

    @Test
    fun buildIfUserLocationIsPresent() {
        whenever(YLocation.createWithOriginalProvider(location)).thenReturn(yLocation)
        whenever(GlobalServiceLocator.getInstance().locationClientApi.systemLocation).thenReturn(mock<Location>())

        val model = factory.create()

        ObjectPropertyAssertions(model)
            .checkField("enabled", true)
            .checkField("latitude", latitude)
            .checkField("longitude", longitude)
            .checkField("timestamp", timestamp)
            .checkField("precision", precision)
            .checkField("direction", direction)
            .checkField("speed", speed)
            .checkField("altitude", altitude)
            .checkField("provider", provider)
            .checkField("originalProvider", originalProvider)
            .checkAll()
    }

    @Test
    fun buildIfOnlySystemLocationIsPresent() {
        whenever(YLocation.createWithoutOriginalProvider(location)).thenReturn(yLocation)
        whenever(GlobalServiceLocator.getInstance().locationClientApi.userLocation).thenReturn(null)
        whenever(GlobalServiceLocator.getInstance().locationClientApi.systemLocation).thenReturn(location)

        val model = factory.create()

        ObjectPropertyAssertions(model)
            .checkField("enabled", true)
            .checkField("latitude", latitude)
            .checkField("longitude", longitude)
            .checkField("timestamp", timestamp)
            .checkField("precision", precision)
            .checkField("direction", direction)
            .checkField("speed", speed)
            .checkField("altitude", altitude)
            .checkField("provider", provider)
            .checkField("originalProvider", originalProvider)
            .checkAll()
    }

    @Test
    fun buildIfLocationIsNull() {
        whenever(GlobalServiceLocator.getInstance().locationClientApi.userLocation).thenReturn(null)
        whenever(GlobalServiceLocator.getInstance().locationClientApi.systemLocation).thenReturn(null)
        val model = factory.create()
        yLocationRule.staticMock.verifyNoInteractions()

        ObjectPropertyAssertions(model)
            .checkField("enabled", true)
            .checkFieldsAreNull(
                "latitude", "longitude", "timestamp", "precision", "direction", "speed", "altitude",
                "provider", "originalProvider"
            )
            .checkAll()
    }

    @Test
    fun buildIfLocationTrackingEnabled() {
        val model = factory.create()
        assertThat(model.enabled).isTrue()
    }

    @Test
    fun buildIfLocationTrackingDisabled() {
        whenever(reportRequestConfig.isLocationTracking).thenReturn(false)
        val model = factory.create()
        assertThat(model.enabled).isFalse()
    }
}
