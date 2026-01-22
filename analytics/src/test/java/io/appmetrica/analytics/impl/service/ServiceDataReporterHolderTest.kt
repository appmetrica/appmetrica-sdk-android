package io.appmetrica.analytics.impl.service

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class ServiceDataReporterHolderTest : CommonTest() {

    private val serviceDataReporterHolder = ServiceDataReporterHolder()

    @Test
    fun registerAndGetServiceDataReporter() {
        val firstType = 1
        val firstServiceDataReporter = mock<ServiceDataReporter>()
        val secondServiceDataReporter = mock<ServiceDataReporter>()

        serviceDataReporterHolder.registerServiceDataReporter(firstType, firstServiceDataReporter)
        serviceDataReporterHolder.registerServiceDataReporter(firstType, secondServiceDataReporter)

        assertThat(serviceDataReporterHolder.getServiceDataReporters(firstType)).containsExactly(
            firstServiceDataReporter, secondServiceDataReporter
        )
    }

    @Test
    fun getServiceDataReportersIfEmpty() {
        val secondType = 2

        assertThat(serviceDataReporterHolder.getServiceDataReporters(secondType)).isEmpty()
    }
}
