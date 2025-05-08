package io.appmetrica.analytics.coreutils.internal

import android.location.Location
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationUtilsTest : CommonTest() {

    @Test
    fun locationToBytesAndBackConversion() {
        val location = Location("gps").apply {
            latitude = 13.2343
            longitude = 64.12312
            altitude = 102.123
            accuracy = 23f
        }
        assertThat(LocationUtils.bytesToLocation(LocationUtils.locationToBytes(location))).isEqualTo(location)
    }

    @Test
    fun `locationToBytes for null`() {
        assertThat(LocationUtils.locationToBytes(null)).isNull()
    }

    @Test
    fun `bytesToLocation for null`() {
        assertThat(LocationUtils.bytesToLocation(null)).isNull()
    }

    @Test
    fun `bytesToLocation for wrong`() {
        assertThat(LocationUtils.bytesToLocation(ByteArray(5) { index -> index.toByte() })).isNull()
    }

    @Test
    fun `bytesToLocation for empty`() {
        assertThat(LocationUtils.bytesToLocation(ByteArray(0))).isNull()
    }
}
