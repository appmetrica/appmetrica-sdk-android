package io.appmetrica.analytics.impl.preparer

import android.location.LocationManager
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class FullLocationInfoComposerTest : CommonTest() {

    private var composer = FullLocationInfoComposer()

    @Test
    fun getLocation() {
        val lon = 45.232
        val lat = 89.243
        val altitude = 9
        val direction = 6
        val precision = 7
        val speed = 10
        val timestamp = 896797L
        val timestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(timestamp)
        val provider = "gps"
        val originalProvider = "network"

        val model = DbLocationModel(
            enabled = null,
            latitude = lat,
            longitude = lon,
            timestamp = timestamp,
            precision = precision,
            direction = direction,
            speed = speed,
            altitude = altitude,
            provider = provider,
            originalProvider = originalProvider,
        )
        val proto = composer.getLocation(model)

        ProtoObjectPropertyAssertions(proto)
            .withIgnoredFields("provider")
            .checkField("lon", lon)
            .checkField("lat", lat)
            .checkField("altitude", altitude)
            .checkField("direction", direction)
            .checkField("precision", precision)
            .checkField("speed", speed)
            .checkField("timestamp", timestampInSeconds)
            .checkField("originalProvider", originalProvider)
            .checkAll()
    }

    @Test
    fun getLocationIfNullModel() {
        val model = null
        val proto = composer.getLocation(model)

        assertThat(proto).isNull()
    }

    @Test
    fun getLocationWithMissingFields() {
        val lon = 45.232
        val lat = 89.243

        val model = DbLocationModel(
            enabled = null,
            latitude = lat,
            longitude = lon,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = null,
            originalProvider = null,
        )
        val proto = composer.getLocation(model)

        ProtoObjectPropertyAssertions(proto)
            .checkField("lon", lon)
            .checkField("lat", lat)
            .checkField("altitude", 0)
            .checkField("direction", 0)
            .checkField("precision", 0)
            .checkField("speed", 0)
            .checkField("timestamp", 0L)
            .checkField("provider", 0)
            .checkField("originalProvider", "")
            .checkAll()
    }

    @Test
    fun getLocationWithMissingLonAndLat() {
        val model = DbLocationModel(
            enabled = null,
            latitude = null,
            longitude = null,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = null,
            originalProvider = null,
        )
        val proto = composer.getLocation(model)

        assertThat(proto).isNull()
    }

    @Test
    fun getLocationIfGpsProvider() {
        val lon = 45.232
        val lat = 89.243

        val model = DbLocationModel(
            enabled = null,
            latitude = lat,
            longitude = lon,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = LocationManager.GPS_PROVIDER,
            originalProvider = null,
        )
        val proto = composer.getLocation(model)

        assertThat(proto?.provider).isEqualTo(EventProto.ReportMessage.Location.PROVIDER_GPS)
    }

    @Test
    fun getLocationIfNetworkProvider() {
        val lon = 45.232
        val lat = 89.243

        val model = DbLocationModel(
            enabled = null,
            latitude = lat,
            longitude = lon,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = LocationManager.NETWORK_PROVIDER,
            originalProvider = null,
        )
        val proto = composer.getLocation(model)

        assertThat(proto?.provider).isEqualTo(EventProto.ReportMessage.Location.PROVIDER_NETWORK)
    }
}
