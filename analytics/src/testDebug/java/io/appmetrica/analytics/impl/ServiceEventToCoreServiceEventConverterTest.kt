package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.charset.StandardCharsets

internal class ServiceEventToCoreServiceEventConverterTest : CommonTest() {

    private val type = 12
    private val customType = 34
    private val name = "some name"
    private val value = "some value"
    private val valueBytes = "some value bytes".toByteArray()
    private val valueProtocolVersion = 2
    private val bytesTruncated = 5

    private val serviceEvent: ServiceEvent = mock {
        on { type } doReturn type
        on { customType } doReturn customType
        on { name } doReturn name
        on { value } doReturn value
        on { valueProtocolVersion } doReturn valueProtocolVersion
        on { bytesTruncated } doReturn bytesTruncated
        on { extras } doReturn mutableMapOf()
    }

    @Test
    fun convert() {
        val coreServiceEvent = ServiceEventToCoreServiceEventConverter.convert(serviceEvent)

        assertSoftly {
            it.assertThat(coreServiceEvent.type).isEqualTo(type)
            it.assertThat(coreServiceEvent.customType).isEqualTo(customType)
            it.assertThat(coreServiceEvent.name).isEqualTo(name)
            it.assertThat(coreServiceEvent.value).isEqualTo(value)
            it.assertThat(coreServiceEvent.valueProtocolVersion).isEqualTo(valueProtocolVersion)
            it.assertThat(coreServiceEvent.bytesTruncated).isEqualTo(bytesTruncated)
            it.assertAll()
        }
    }

    @Test
    fun convertIfHasValueBytes() {
        whenever(serviceEvent.valueBytes).thenReturn(valueBytes)

        val coreServiceEvent = ServiceEventToCoreServiceEventConverter.convert(serviceEvent)

        assertSoftly {
            it.assertThat(coreServiceEvent.type).isEqualTo(type)
            it.assertThat(coreServiceEvent.name).isEqualTo(name)
            it.assertThat(coreServiceEvent.valueBytes).isEqualTo(valueBytes)
            it.assertThat(coreServiceEvent.value)
                .isEqualTo(String(valueBytes, StandardCharsets.UTF_8))
            it.assertAll()
        }
    }

    @Test
    fun convertWithPrototype() {
        val prototype = CoreServiceEvent().apply {
            profileID = "profile"
        }

        val coreServiceEvent = ServiceEventToCoreServiceEventConverter.convert(serviceEvent, prototype)

        assertSoftly {
            it.assertThat(coreServiceEvent.type).isEqualTo(type)
            it.assertThat(coreServiceEvent.name).isEqualTo(name)
            it.assertThat(coreServiceEvent.value).isEqualTo(value)
            it.assertThat(coreServiceEvent.profileID).isEqualTo(prototype.profileID)
            it.assertAll()
        }
    }
}
