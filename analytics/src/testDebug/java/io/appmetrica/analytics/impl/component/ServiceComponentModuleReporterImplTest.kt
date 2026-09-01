package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent
import io.appmetrica.analytics.impl.CoreServiceEvent
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class ServiceComponentModuleReporterImplTest : CommonTest() {

    private val type = 12
    private val name = "event name"

    private val serviceEvent: ServiceEvent = mock {
        on { type } doReturn type
        on { customType } doReturn 0
        on { name } doReturn name
        on { bytesTruncated } doReturn 0
        on { extras } doReturn mutableMapOf()
    }
    private val componentUnit: ComponentUnit = mock()

    private val reporter = ServiceComponentModuleReporterImpl(componentUnit)

    @Test
    fun handleReport() {
        reporter.handleReport(serviceEvent)

        val captor = argumentCaptor<CoreServiceEvent>()
        verify(componentUnit).handleReport(captor.capture())
        assertThat(captor.firstValue.type).isEqualTo(type)
        assertThat(captor.firstValue.name).isEqualTo(name)
    }
}
