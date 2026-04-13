package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider
import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ReportMessagePreparerTest : CommonTest() {

    private val queryValues = mapOf("key" to "value")
    private val requestConfig = mock<ReportRequestConfig>()
    private val dbRequestConfig = mock<DbNetworkTaskConfig>()
    private val certificates = listOf("cert1")
    private val sessionId = 42L
    private val requestId = 7

    private val dbInteractor = mock<ReportTaskDbInteractor> {
        on { getNextRequestId() } doReturn requestId
    }
    private val trimmer = mock<Trimmer<ByteArray>>()
    private val selfReporter = mock<IReporterExtended>()
    private val telephonyDataProvider = mock<TelephonyDataProvider>()

    private val emptySessionData = ReportSessionData(emptyList(), emptyList(), JSONObject())
    private val sessionData = ReportSessionData(
        sessions = listOf(EventProto.ReportMessage.Session()),
        internalSessionsIds = listOf(sessionId),
        environment = JSONObject(),
    )
    private val builtMessage = EventProto.ReportMessage()

    @get:Rule
    val collectorMockedRule = MockedConstructionRule(ReportSessionsCollector::class.java)

    @get:Rule
    val builderMockedRule = MockedConstructionRule(ReportMessageBuilder::class.java)

    private val preparer by setUp {
        ReportMessagePreparer(dbInteractor, trimmer, selfReporter, telephonyDataProvider)
    }

    private val collector get() = collectorMockedRule.constructionMock.constructed()[0]
    private val builder get() = builderMockedRule.constructionMock.constructed()[0]

    @Test
    fun `prepare returns null and does not call builder when sessions are empty`() {
        whenever(collector.collect(queryValues, requestConfig)).thenReturn(emptySessionData)

        val result = preparer.prepare(queryValues, requestConfig, certificates, dbRequestConfig)

        assertThat(result).isNull()
        verify(builder, never()).build(any(), any(), any(), any())
        verify(dbInteractor, never()).getNextRequestId()
    }

    @Test
    fun `prepare returns PreparedReport with message, sessionIds and requestId on happy path`() {
        whenever(collector.collect(queryValues, requestConfig)).thenReturn(sessionData)
        whenever(builder.build(sessionData, dbRequestConfig, requestConfig, certificates)).thenReturn(builtMessage)

        val result = preparer.prepare(queryValues, requestConfig, certificates, dbRequestConfig)

        assertThat(result).isNotNull
        assertThat(result!!.reportMessage).isSameAs(builtMessage)
        assertThat(result.internalSessionsIds).containsExactly(sessionId)
        assertThat(result.requestId).isEqualTo(requestId)
    }
}
