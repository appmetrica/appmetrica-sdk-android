package io.appmetrica.analytics.impl.utils.encryption

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.InternalEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class EventEncrypterProviderByEventTypeTests(
    private val eventTypeId: Int,
    private val eventEncrypterClassName: String?,
    @Suppress("unused") eventTypeCaption: String?
) : EventEncrypterProviderBaseTest() {

    @Test
    fun returnExpectedEncryption() {
        val counterReport = CounterReport()
        counterReport.type = eventTypeId
        assertThat(eventEncrypterProvider.getEventEncrypter(counterReport).javaClass.getName())
            .isEqualTo(eventEncrypterClassName)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "Return {1} for eventType = {2}")
        fun data(): Collection<Array<Any?>> {
            val eventTypeToEncrypterMapping = mutableMapOf<InternalEvents, String?>()
            val data = mutableListOf<Array<Any?>>()
            for (eventType in InternalEvents.entries) {
                val encrypterClassName = eventTypeToEncrypterMapping[eventType]
                data.add(
                    arrayOf(
                        eventType.typeId,
                        encrypterClassName ?: DummyEventEncrypter::class.java.getName(),
                        eventType.name
                    )
                )
            }
            return data
        }
    }
}
