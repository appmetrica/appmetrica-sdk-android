package io.appmetrica.analytics.impl.events

import android.content.Context
import io.appmetrica.analytics.ModuleEvent.Category
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.adrevenue.NativeLayerPayloadEnricher
import io.appmetrica.analytics.impl.adrevenue.SupportedAdNetworksPayloadEnricher
import io.appmetrica.analytics.impl.adrevenue.YandexSourcePayloadEnricher
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LibraryEventConstructorTest : CommonTest() {

    private val sender = "sender_value"
    private val event = "event_value"
    private val payload = "payload_value"
    private val nativeEnricherPayloadKey = "native enricher payload key"
    private val nativeEnricherPayloadValue = "native enricher payload value"
    private val generalEnricherPayloadKey = "general enricher payload key"
    private val generalEnricherPayloadValue = "general enricher payload value"
    private val yandexSourcePayloadKey = "yandex source payload key"
    private val yandexSourcePayloadValue = "yandex source payload value"

    @get:Rule
    val nativeLayerPayloadEnricherRule = constructionRule<NativeLayerPayloadEnricher>() {
        on { enrich(any()) } doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val inputPayload = invocation.arguments[0] as MutableMap<String, String>
            inputPayload[nativeEnricherPayloadKey] = nativeEnricherPayloadValue
            inputPayload
        }
    }

    @get:Rule
    val supportedAdNetworksPayloadEnricherRule = constructionRule<SupportedAdNetworksPayloadEnricher> {
        on { enrich(any()) } doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val inputPayload = invocation.arguments[0] as MutableMap<String, String>
            inputPayload[generalEnricherPayloadKey] = generalEnricherPayloadValue
            inputPayload
        }
    }

    @get:Rule
    val yandexSourcePayloadEnricherRule = constructionRule<YandexSourcePayloadEnricher> {
        on { enrich(any()) } doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val inputPayload = invocation.arguments[0] as MutableMap<String, String>
            inputPayload[yandexSourcePayloadKey] = yandexSourcePayloadValue
            inputPayload
        }
    }

    private val context: Context = mock()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val constructor by setUp {
        whenever(ClientServiceLocator.getInstance().contextAppearedListener.peekContext()).doReturn(context)
        LibraryEventConstructor()
    }

    @Test
    fun constructEvent() {
        val moduleEvent = constructor.constructEvent(sender, event, payload)

        ObjectPropertyAssertions(moduleEvent)
            .withPrivateFields(true)
            .checkField("type", EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .checkField("name", "appmetrica_system_event_42")
            .checkFieldIsNull("value")
            .checkField("serviceDataReporterType", 1)
            .checkField("category", Category.GENERAL)
            .checkFieldIsNull("environment")
            .checkFieldIsNull("extras")
            .checkFieldRecursively<List<Map.Entry<String, Any>>>("attributes") {
                assertThat(it.actual).containsExactlyInAnyOrderElementsOf(
                    mapOf(
                        "sender" to sender,
                        "event" to event,
                        "payload" to payload,
                        nativeEnricherPayloadKey to nativeEnricherPayloadValue,
                        generalEnricherPayloadKey to generalEnricherPayloadValue,
                        yandexSourcePayloadKey to yandexSourcePayloadValue
                    ).entries.toList()
                )
            }
            .checkAll()
    }

    @Test
    fun `constructEvent for null values`() {
        val moduleEvent = constructor.constructEvent(null, null, null)

        ObjectPropertyAssertions(moduleEvent)
            .withPrivateFields(true)
            .checkField("type", EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .checkField("name", "appmetrica_system_event_42")
            .checkFieldIsNull("value")
            .checkField("serviceDataReporterType", 1)
            .checkField("category", Category.GENERAL)
            .checkFieldIsNull("environment")
            .checkFieldIsNull("extras")
            .checkFieldRecursively<List<Map.Entry<String, Any>>>("attributes") {
                assertThat(it.actual).containsExactlyInAnyOrderElementsOf(
                    mapOf(
                        "sender" to "null",
                        "event" to "null",
                        "payload" to "null",
                        nativeEnricherPayloadKey to nativeEnricherPayloadValue,
                        generalEnricherPayloadKey to generalEnricherPayloadValue,
                        yandexSourcePayloadKey to yandexSourcePayloadValue
                    ).entries.toList()
                )
            }
            .checkAll()
    }

    @Test
    fun enrichers() {
        constructor.constructEvent(sender, event, payload)
        constructor.constructEvent(sender, event, payload)
        assertThat(nativeLayerPayloadEnricherRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeLayerPayloadEnricherRule.argumentInterceptor.flatArguments()).isEmpty()
        assertThat(supportedAdNetworksPayloadEnricherRule.constructionMock.constructed()).hasSize(1)
        assertThat(supportedAdNetworksPayloadEnricherRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        assertThat(yandexSourcePayloadEnricherRule.constructionMock.constructed()).hasSize(1)
        assertThat(yandexSourcePayloadEnricherRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
