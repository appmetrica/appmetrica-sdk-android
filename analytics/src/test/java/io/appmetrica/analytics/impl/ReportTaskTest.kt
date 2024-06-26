package io.appmetrica.analytics.impl

import android.content.ContentValues
import android.database.MatrixCursor
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.io.GZIPCompressor
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventDescriptionToBytesConverter
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionDescriptionToBytesConverter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.events.EventTrigger
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.utils.TimeUtils
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.impl.utils.limitation.BytesTrimmer
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.networktasks.internal.DefaultNetworkResponseHandler
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncrypter
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder
import io.appmetrica.analytics.networktasks.internal.SendingDataTaskHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ConstructionArgumentCaptor
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

@RunWith(RobolectricTestRunner::class)
internal class ReportTaskTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val selfReporterFacadeMockedRule = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    val sendingTaskHelperConstructorCaptor = ConstructionArgumentCaptor<SendingDataTaskHelper>()

    @get:Rule
    val sendingTaskHelperMockedRule = MockedConstructionRule(
        SendingDataTaskHelper::class.java, sendingTaskHelperConstructorCaptor
    )

    @get:Rule
    val gzipCompressorMockedRule = MockedConstructionRule(GZIPCompressor::class.java)

    @get:Rule
    val defaultNetworkResponseHandlerMockedRule =
        MockedConstructionRule(DefaultNetworkResponseHandler::class.java)

    private val contentValuesKey = "contentValuesKey"
    private val contentValuesValue = "contentValuesValue"
    private val firstQueryParameter = ContentValues().apply {
        put(contentValuesKey, contentValuesValue)
    }
    private val queryParameters = listOf(firstQueryParameter)

    private val columnReport = arrayOf(
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION
    )
    private val columnSession = arrayOf(
        Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID,
        Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE,
        Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS,
        Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION,
    )

    val eventValue = "event value without truncation"
    val truncatedValue = "truncated event value"
    val sessionId = 1L
    val type = 0
    val sessionCursor = MatrixCursor(columnSession).apply {
        newRow()
            .add(sessionId) // FIELD_SESSION_ID
            .add(type) // FIELD_SESSION_TYPE
            .add("") // FIELD_SESSION_REPORT_REQUEST_PARAMETERS
            .add(DbSessionDescriptionToBytesConverter().fromModel(DbSessionModel.Description(
                startTime = TimeUtils.currentDeviceTimeSec(),
                serverTimeOffset = 10,
                obtainedBeforeFirstSynchronization = true
            ))) // FIELD_SESSION_DESCRIPTION
    }
    val reportCursor = MatrixCursor(columnReport).apply {
        newRow()
            .add(sessionId) // FIELD_REPORT_SESSION
            .add(SessionType.BACKGROUND.code) // FIELD_REPORT_SESSION_TYPE
            .add(0) // FIELD_REPORT_NUMBER_IN_SESSION
            .add(InternalEvents.EVENT_TYPE_REGULAR.typeId) // FIELD_REPORT_TYPE
            .add(10) // FIELD_REPORT_GLOBAL_NUMBER
            .add(0) // FIELD_REPORT_TIME
            .add(DbEventDescriptionToBytesConverter().fromModel(DbEventModel.Description(
                customType = null,
                name = null,
                value = eventValue,
                numberOfType = 21L,
                locationInfo = null,
                errorEnvironment = null,
                appEnvironment = "{}",
                appEnvironmentRevision = 0,
                truncated = 0,
                connectionType = 0,
                cellularConnectionType = "0",
                encryptingMode = EventEncryptionMode.NONE,
                profileId = profileId,
                firstOccurrenceStatus = null,
                source = null,
                attributionIdChanged = null,
                openId = null,
                extras = null
            ))) // FIELD_EVENT_DESCRIPTION
    }

    private val databaseHelper = mock<DatabaseHelper> {
        on { collectAllQueryParameters() } doReturn queryParameters
        on { querySessions(any()) } doReturn sessionCursor
        on { queryReports(any(), any()) } doAnswer {
            reportCursor.moveToPosition(-1)
            reportCursor
        }
    }
    private val publicLogger = mock<PublicLogger>()
    private val prevReportRequestId = 14
    private val vitalComponentDataProvider = mock<VitalComponentDataProvider> {
        on { reportRequestId } doReturn prevReportRequestId
    }

    private val apiKey = UUID.randomUUID().toString()
    private val anonymizedApiKey = UUID.randomUUID().toString()

    private val componentId = mock<ComponentId> {
        on { apiKey } doReturn apiKey
        on { anonymizedApiKey } doReturn anonymizedApiKey
    }

    private val sessionRemovingThreshold = 5L
    private val sessionManager = mock<SessionManagerStateMachine> {
        on { thresholdSessionIdForActualSessions } doReturn sessionRemovingThreshold
    }

    private val eventTrigger = mock<EventTrigger>()

    private val componentUnit = mock<ComponentUnit> {
        on { dbHelper } doReturn databaseHelper
        on { publicLogger } doReturn publicLogger
        on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
        on { componentId } doReturn componentId
        on { sessionManager } doReturn sessionManager
        on { eventTrigger } doReturn eventTrigger
    }
    private val reportParamsAppender = mock<ReportParamsAppender>()

    private val firstCertificate = "First certificate"
    private val secondCertificate = "Second certificate"
    private val certificates = listOf(firstCertificate, secondCertificate)

    private val firstHost = "first host"
    private val secondHost = "second host"
    private val reportHosts = listOf(firstHost, secondHost)

    private val profileId = "ProfileId"

    private val uuid = UUID.randomUUID().toString()
    private val deviceId = UUID.randomUUID().toString()

    private val reportRequestConfig = mock<ReportRequestConfig> {
        on { certificates } doReturn certificates
        on { reportHosts } doReturn reportHosts
        on { isReadyForSending } doReturn true
        on { locale } doReturn "ru"
        on { uuid } doReturn uuid
        on { deviceId } doReturn deviceId
    }
    private val lazyReportConfigProvider = mock<LazyReportConfigProvider> {
        on { config } doReturn reportRequestConfig
    }
    private val fullUrlFormer = mock<FullUrlFormer<ReportRequestConfig>>() {
        on { allHosts } doReturn reportHosts
    }
    private val requestDataHolder = mock<RequestDataHolder>()
    private val responseDataHolder = mock<ResponseDataHolder>()
    private val requestBodyEncrypter = mock<RequestBodyEncrypter>()
    private val selfReporter = mock<IReporterExtended>()
    private val bytesTrimmer = mock<BytesTrimmer> {
        on { trim(eventValue.toByteArray()) } doReturn truncatedValue.toByteArray()
    }

    private lateinit var reportTask: ReportTask

    @Before
    fun setUp() {
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter)
        reportTask = ReportTask(
            componentUnit,
            publicLogger,
            databaseHelper,
            reportParamsAppender,
            vitalComponentDataProvider,
            lazyReportConfigProvider,
            bytesTrimmer,
            selfReporter,
            fullUrlFormer,
            requestDataHolder,
            responseDataHolder,
            requestBodyEncrypter
        )
    }

    @Test
    fun constructor() {
        ObjectPropertyAssertions(
            ReportTask(
                componentUnit,
                reportParamsAppender,
                lazyReportConfigProvider,
                fullUrlFormer,
                requestDataHolder,
                responseDataHolder,
                requestBodyEncrypter
            )
        )
            .withPrivateFields(true)
            .withIgnoredFields("mQueryValues")
            .checkField("mComponent", componentUnit)
            .checkField("mDbHelper", databaseHelper)
            .checkFieldRecursively<BytesTrimmer>("mTrimmer") { bytesTrimmerAssertions ->
                bytesTrimmerAssertions
                    .withPrivateFields(true)
                    .includingParents(true)
                    .checkField("mMaxSize", EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE)
                    .checkField("mLogName", "event value in ReportTask")
                    .checkField("mPublicLogger", publicLogger)
                    .checkAll()
            }
            .checkField("mPublicLogger", publicLogger)
            .checkField("vitalComponentDataProvider", vitalComponentDataProvider)
            .checkField("mSelfReporter", selfReporter)
            .checkField("paramsAppender", reportParamsAppender)
            .checkField("fullUrlFormer", fullUrlFormer)
            .checkField("configProvider", lazyReportConfigProvider)
            .checkField("requestDataHolder", requestDataHolder)
            .checkField("responseDataHolder", responseDataHolder)
            //One more sendingDataTaskHelper initialized in setUp()
            .checkField(
                "sendingDataTaskHelper",
                sendingTaskHelperMockedRule.constructionMock.constructed()[1]
            ).also {
                assertThat(sendingTaskHelperConstructorCaptor.arguments[1])
                    .containsExactly(
                        requestBodyEncrypter,
                        gzipCompressorMockedRule.constructionMock.constructed()[1],
                        requestDataHolder,
                        responseDataHolder,
                        defaultNetworkResponseHandlerMockedRule.constructionMock.constructed()[1]
                    )
            }
            .checkAll()
    }

    @Test
    fun onCreateTask() {
        assertThat(reportTask.onCreateTask()).isTrue()
        verify(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
            .prepareAndSetPostData(any())
        verify(reportParamsAppender).setRequestId((prevReportRequestId + 1).toLong())
    }

    @Test
    fun onCreateTaskIfQueryParametersIsEmpty() {
        whenever(databaseHelper.collectAllQueryParameters()).thenReturn(emptyList())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateTaskIfNoCertificated() {
        whenever(reportRequestConfig.certificates).thenReturn(null)
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateTaskIfCertificatedIsEmpty() {
        whenever(reportRequestConfig.certificates).thenReturn(emptyList())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfNotReadyForSending() {
        whenever(reportRequestConfig.isReadyForSending).thenReturn(false)
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfNullHosts() {
        whenever(fullUrlFormer.allHosts).thenReturn(null)
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfEmptyHosts() {
        whenever(fullUrlFormer.allHosts).thenReturn(emptyList())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfSessionsCursorIsNull() {
        whenever(databaseHelper.querySessions(any())).thenReturn(null)
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfSessionsCursorIsEmpty() {
        whenever(databaseHelper.querySessions(any())).thenReturn(MatrixCursor(columnSession))
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfEventsCursorIsNull() {
        whenever(databaseHelper.queryReports(any(), any())).thenReturn(null)
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfEventsCursorIsEmpty() {
        whenever(databaseHelper.queryReports(any(), any())).thenReturn(MatrixCursor(columnReport))
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreatePostData() {
        reportTask.onCreateTask()
        val bodyCaptor = ArgumentCaptor.forClass(ByteArray::class.java)
        verify(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
            .prepareAndSetPostData(bodyCaptor.capture())
        val body = bodyCaptor.value
        val reportMessage = EventProto.ReportMessage.parseFrom(body)
        val assertions = SoftAssertions()
        with(assertions) {
            assertThat(reportMessage.reportRequestParameters.uuid)
                .`as`("uuid")
                .isEqualTo(uuid)
            assertThat(reportMessage.reportRequestParameters.deviceId)
                .`as`(deviceId)
                .isEqualTo(deviceId)
            assertThat(reportMessage.sessions.size)
                .`as`("session count")
                .isEqualTo(1)
            assertThat(reportMessage.sessions[0].events.size)
                .`as`("events count")
                .isEqualTo(1)
            assertThat(reportMessage.sessions[0].events[0].value)
                .`as`("Event value")
                .isEqualTo(truncatedValue.toByteArray())
            assertThat(reportMessage.sessions[0].events[0].type)
                .`as`("Event type")
                .isEqualTo(EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            assertThat(reportMessage.sessions[0].id).isEqualTo(sessionId)
            assertAll()
        }
    }

    @Test
    fun onPerformRequest() {
        reportTask.onPerformRequest()
        verify(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
            .onPerformRequest()
    }

    @Test
    fun onRequestCompleteForTrue() = onRequestComplete(true)

    @Test
    fun onRequestCompleteForFalse() = onRequestComplete(false)

    private fun onRequestComplete(status: Boolean) {
        whenever(sendingTaskHelperMockedRule.constructionMock.constructed()[0].isResponseValid)
            .thenReturn(status)
        assertThat(reportTask.onRequestComplete()).isEqualTo(status)
    }

    @Test
    fun onPostRequestCompleteForSuccess() {
        reportTask.onCreateTask()
        reportTask.onPostRequestComplete(true)
        inOrder(databaseHelper) {
            verify(databaseHelper).removeTop(sessionId, SessionType.FOREGROUND.code, reportCursor.count, false)
            verify(databaseHelper).removeEmptySessions(sessionRemovingThreshold)
        }
    }

    @Test
    fun onPostRequestCompleteForBadRequest() {
        whenever(responseDataHolder.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
        reportTask.onCreateTask()
        reportTask.onPostRequestComplete(false)
        inOrder(databaseHelper) {
            verify(databaseHelper).removeTop(sessionId, SessionType.FOREGROUND.code, reportCursor.count, true)
            verify(databaseHelper).removeEmptySessions(sessionRemovingThreshold)
        }
    }

    @Test
    fun onPostRequestCompleteWithNonBadRequestError() {
        reportTask.onCreateTask()
        reportTask.onPostRequestComplete(false)
        verify(databaseHelper, never()).removeTop(any(), any(), any(), any())
        verify(databaseHelper, never()).removeEmptySessions(any())
    }

    @Test
    fun onTaskAdded() {
        reportTask.onTaskAdded()
        verify(eventTrigger).disableTrigger()
    }

    @Test
    fun onTaskFinished() {
        reportTask.onTaskFinished()
        verify(databaseHelper).clearIfTooManyEvents()
        verify(eventTrigger).enableTrigger()
    }

    @Test
    fun onTaskRemoved() {
        reportTask.onTaskRemoved()
        verify(eventTrigger).enableTrigger()
    }

    @Test
    fun onSuccessfulTaskFinished() {
        reportTask.onSuccessfulTaskFinished()
        verify(eventTrigger).trigger()
    }

    @Test
    fun description() {
        assertThat(reportTask.description()).contains(anonymizedApiKey)
    }

    @Test
    fun eventCountLimitation() {
        val sessionCursorWithLargeAmountOfEvents = MatrixCursor(columnSession).apply {
            repeat(100) {
                newRow()
                    .add(it) // FIELD_SESSION_ID
                    .add(type) // FIELD_SESSION_TYPE
                    .add("") // FIELD_SESSION_REPORT_REQUEST_PARAMETERS
                    .add(DbSessionDescriptionToBytesConverter().fromModel(DbSessionModel.Description(
                        startTime = TimeUtils.currentDeviceTimeSec() + it,
                        serverTimeOffset = 10L * it,
                        obtainedBeforeFirstSynchronization = true
                    ))) // FIELD_SESSION_DESCRIPTION
            }
        }
        val reportCursorWithLargeAmountOfEvents = MatrixCursor(columnReport).apply {
            repeat(1000) {
                newRow()
                    .add(sessionId) // FIELD_REPORT_SESSION
                    .add(SessionType.BACKGROUND.code) // FIELD_REPORT_SESSION_TYPE
                    .add(it) // FIELD_REPORT_NUMBER_IN_SESSION
                    .add(InternalEvents.EVENT_TYPE_REGULAR.typeId) // FIELD_REPORT_TYPE
                    .add(it) // FIELD_REPORT_GLOBAL_NUMBER
                    .add(it) // FIELD_REPORT_TIME
                    .add(DbEventDescriptionToBytesConverter().fromModel(DbEventModel.Description(
                        customType = null,
                        name = null,
                        value = eventValue,
                        numberOfType = it.toLong(),
                        locationInfo = null,
                        errorEnvironment = null,
                        appEnvironment = "{}",
                        appEnvironmentRevision = 0,
                        truncated = 0,
                        connectionType = 0,
                        cellularConnectionType = "0",
                        encryptingMode = EventEncryptionMode.NONE,
                        profileId = profileId,
                        firstOccurrenceStatus = null,
                        source = null,
                        attributionIdChanged = null,
                        openId = null,
                        extras = null
                    ))) // FIELD_EVENT_DESCRIPTION
            }
        }
        stubbing(databaseHelper) {
            on { collectAllQueryParameters() } doReturn queryParameters
            on { querySessions(any()) } doReturn sessionCursorWithLargeAmountOfEvents
            on { queryReports(any(), any()) } doReturn reportCursorWithLargeAmountOfEvents
        }

        reportTask.onCreateTask()
        val bodyCaptor = ArgumentCaptor.forClass(ByteArray::class.java)
        verify(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
            .prepareAndSetPostData(bodyCaptor.capture())
        val body = bodyCaptor.value
        val reportMessage = EventProto.ReportMessage.parseFrom(body)
        assertThat(reportMessage.sessions).hasSize(1)
        assertThat(reportMessage.sessions.first().events).hasSize(100)
    }
}
