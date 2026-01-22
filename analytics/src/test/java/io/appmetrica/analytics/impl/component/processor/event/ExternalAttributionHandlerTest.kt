package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.attribution.ExternalAttributionHelper
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ExternalAttributionHandlerTest : CommonTest() {

    private val valueBytes = "string".toByteArray()
    private val counterReport: CounterReport = mock {
        on { valueBytes } doReturn valueBytes
    }

    private val type = 12
    private val json = "json"
    private val clientExternalAttribution = ClientExternalAttribution().apply {
        attributionType = type
        value = json.toByteArray()
    }

    private val publicLogger: PublicLogger = mock()
    private val componentUnit: ComponentUnit = mock {
        on { publicLogger } doReturn publicLogger
    }
    private val timeProvider: TimeProvider = mock()

    @get:Rule
    val externalAttributionHelperRule = MockedConstructionRule(ExternalAttributionHelper::class.java) { mock, _ ->
        whenever(mock.isInAttributionCollectingWindow()).thenReturn(true)
        whenever(mock.isNewAttribution(type, json)).thenReturn(true)
    }

    @get:Rule
    val clientExternalAttributionRule =
        MockedStaticRule(ClientExternalAttribution::class.java)

    private lateinit var handler: ExternalAttributionHandler

    @Before
    fun setUp() {
        handler = ExternalAttributionHandler(componentUnit, timeProvider)

        whenever(ClientExternalAttribution.parseFrom(valueBytes))
            .thenReturn(clientExternalAttribution)
    }

    @Test
    fun process() {
        val result = handler.process(counterReport)

        verify(externalAttributionHelperRule.constructionMock.constructed().first()).saveAttribution(type, json)
        assertThat(result).isFalse()
    }

    @Test
    fun processIfNotNewAttribution() {
        whenever(externalAttributionHelperRule.constructionMock.constructed().first().isNewAttribution(type, json))
            .thenReturn(false)
        val result = handler.process(counterReport)

        assertThat(result).isTrue()
    }

    @Test
    fun processIfOutOfCollectingInterval() {
        whenever(externalAttributionHelperRule.constructionMock.constructed().first().isInAttributionCollectingWindow())
            .thenReturn(false)
        val result = handler.process(counterReport)

        assertThat(result).isTrue()
    }
}
