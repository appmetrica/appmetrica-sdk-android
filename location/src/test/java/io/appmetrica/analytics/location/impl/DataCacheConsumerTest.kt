package io.appmetrica.analytics.location.impl

import android.location.Location
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DataCacheConsumerTest : CommonTest() {

    private val locationCache = mock<LocationDataCache>()
    private val location = mock<Location>()

    private lateinit var locationCacheConsumer: LocationCacheConsumer

    @Before
    fun setUp() {
        locationCacheConsumer = LocationCacheConsumer(locationCache)
    }

    @Test
    fun nullLocation() {
        locationCacheConsumer.consume(null)
        verifyNoMoreInteractions(locationCache)
    }

    @Test
    fun notNullLocation() {
        locationCacheConsumer.consume(location)
        verify(locationCache).updateData(location)
    }
}
