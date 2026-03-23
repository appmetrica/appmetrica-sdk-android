package io.appmetrica.analytics.impl

import android.content.ContentValues
import android.database.MatrixCursor
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.db.DBUtils
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
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.events.EventTrigger
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
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
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.nio.charset.Charset
import java.util.UUID
import java.util.function.Predicate
import javax.net.ssl.HttpsURLConnection

@RunWith(RobolectricTestRunner::class)
internal class ReportTaskTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val selfReporterFacadeMockedRule = MockedStaticRule(AppMetricaSelfReportFacade::class.java)

    private val sendingTaskHelperConstructorCaptor = ConstructionArgumentCaptor<SendingDataTaskHelper>()

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

    private val columnReport = arrayOf(
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME,
        Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION
    )
    val eventValue = "event value without truncation"
    val truncatedValue = "truncated event value"
    val sessionId = 1L
    val type = 0
    val sessionModel = DbSessionModel(
        id = sessionId,
        type = SessionType.FOREGROUND,
        reportRequestParameters = "",
        description = DbSessionModel.Description(
            startTime = TimeUtils.currentDeviceTimeSec(),
            serverTimeOffset = 10,
            obtainedBeforeFirstSynchronization = true,
        )
    )
    val reportCursor = MatrixCursor(columnReport).apply {
        newRow()
            .add(sessionId) // FIELD_REPORT_SESSION
            .add(SessionType.BACKGROUND.code) // FIELD_REPORT_SESSION_TYPE
            .add(0) // FIELD_REPORT_NUMBER_IN_SESSION
            .add(InternalEvents.EVENT_TYPE_REGULAR.typeId) // FIELD_REPORT_TYPE
            .add(10) // FIELD_REPORT_GLOBAL_NUMBER
            .add(0) // FIELD_REPORT_TIME
            .add(
                DbEventDescriptionToBytesConverter().fromModel(
                    DbEventModel.Description(
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
                    )
                )
            ) // FIELD_EVENT_DESCRIPTION
    }

    private val databaseHelper = mock<DatabaseHelper>()
    private val prevReportRequestId = 14
    private val dbInteractor get() = dbInteractorMockedRule.constructionMock.constructed()[0]

    private val dbInteractorConstructorCaptor = ConstructionArgumentCaptor<ReportTaskDbInteractor>()

    @get:Rule
    val dbInteractorMockedRule = MockedConstructionRule(
        ReportTaskDbInteractor::class.java, dbInteractorConstructorCaptor
    )

    private val bytesTrimmerConstructorCaptor = ConstructionArgumentCaptor<BytesTrimmer>()

    @get:Rule
    val bytesTrimmerMockedRule = MockedConstructionRule(
        BytesTrimmer::class.java, bytesTrimmerConstructorCaptor
    )

    private val publicLogger = mock<PublicLogger>()
    private val vitalComponentDataProvider = mock<VitalComponentDataProvider> {
        on { reportRequestId } doReturn prevReportRequestId
    }

    private val apiKey = UUID.randomUUID().toString()
    private val anonymizedApiKey = UUID.randomUUID().toString()

    private val componentId = mock<ComponentId> {
        on { apiKey } doReturn apiKey
        on { anonymizedApiKey } doReturn anonymizedApiKey
    }

    private val sessionManager = mock<SessionManagerStateMachine>()

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
    private val firstAutoCollectedDataObserver = "First"

    private val reportRequestConfig = mock<ReportRequestConfig> {
        on { certificates } doReturn certificates
        on { reportHosts } doReturn reportHosts
        on { isReadyForSending } doReturn true
        on { locale } doReturn "ru"
        on { uuid } doReturn uuid
        on { deviceId } doReturn deviceId
        on { autoCollectedDataSubscribers } doReturn setOf(firstAutoCollectedDataObserver)
    }
    private val lazyReportConfigProvider = mock<LazyReportConfigProvider> {
        on { config } doReturn reportRequestConfig
    }
    private val fullUrlFormer = mock<FullUrlFormer<ReportRequestConfig>> {
        on { allHosts } doReturn reportHosts
    }
    private val requestDataHolder = mock<RequestDataHolder>()
    private val responseDataHolder = mock<ResponseDataHolder>()
    private val requestBodyEncrypter = mock<RequestBodyEncrypter>()
    private val selfReporter = mock<SelfReporterWrapper>()

    private lateinit var reportTask: ReportTask

    @Before
    fun setUp() {
        whenever(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter)
        reportTask = ReportTask(
            componentUnit,
            reportParamsAppender,
            lazyReportConfigProvider,
            fullUrlFormer,
            requestDataHolder,
            responseDataHolder,
            requestBodyEncrypter
        )
        whenever(dbInteractor.collectAllQueryParameters()).thenReturn(firstQueryParameter)
        whenever(dbInteractor.querySessionModels(any())).thenReturn(listOf(sessionModel))
        whenever(dbInteractor.queryReportsForSessions(any(), any())).doAnswer {
            mapOf(sessionId to cursorToContentValuesList(reportCursor))
        }
        whenever(dbInteractor.getNextRequestId()).thenReturn(prevReportRequestId + 1)
        whenever(bytesTrimmerMockedRule.constructionMock.constructed()[0].trim(eventValue.toByteArray()))
            .thenReturn(truncatedValue.toByteArray())
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
            .withIgnoredFields(
                "mQueryValues",
                "mDbReportRequestConfig",
                "mPreparedReport",
                "mPreparer",
                "mRequestId",
                "shouldTriggerSendingEvents",
            )
            .checkField("mComponent", componentUnit)
            .checkField("mDbInteractor", dbInteractorMockedRule.constructionMock.constructed()[1])
            .also {
                assertThat(bytesTrimmerConstructorCaptor.arguments[1])
                    .containsExactly(
                        EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
                        "event value in ReportTask",
                        publicLogger
                    )
            }
            .checkField("mPublicLogger", publicLogger)
            .checkField("paramsAppender", reportParamsAppender)
            .checkField("fullUrlFormer", fullUrlFormer)
            .checkField("configProvider", lazyReportConfigProvider)
            .checkField("requestDataHolder", requestDataHolder)
            .checkField("responseDataHolder", responseDataHolder)
            // One more sendingDataTaskHelper initialized in setUp()
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
                assertThat(dbInteractorConstructorCaptor.arguments[1])
                    .containsExactly(componentUnit)
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
        whenever(dbInteractor.collectAllQueryParameters()).thenReturn(null)
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
        whenever(dbInteractor.querySessionModels(any())).thenReturn(emptyList())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfSessionsCursorIsEmpty() {
        whenever(dbInteractor.querySessionModels(any())).thenReturn(emptyList())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfNoEventsForSession() {
        whenever(dbInteractor.queryReportsForSessions(any(), any())).thenReturn(emptyMap())
        assertThat(reportTask.onCreateTask()).isFalse()
        verifyNoMoreInteractions(sendingTaskHelperMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun onCreateIfEventsListIsEmpty() {
        whenever(dbInteractor.queryReportsForSessions(any(), any())).thenReturn(mapOf(sessionId to emptyList()))
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
                .describedAs("uuid")
                .isEqualTo(uuid)
            assertThat(reportMessage.reportRequestParameters.deviceId)
                .describedAs(deviceId)
                .isEqualTo(deviceId)
            assertThat(reportMessage.additionalApiKeys)
                .describedAs("additional api keys")
                .matches(
                    Predicate {
                        it.size == 1 && it[0].toString(Charset.defaultCharset()) == firstAutoCollectedDataObserver
                    }
                )
            assertThat(reportMessage.sessions.size)
                .describedAs("session count")
                .isEqualTo(1)
            assertThat(reportMessage.sessions[0].events.size)
                .describedAs("events count")
                .isEqualTo(1)
            assertThat(reportMessage.sessions[0].events[0].value)
                .describedAs("Event value")
                .isEqualTo(truncatedValue.toByteArray())
            assertThat(reportMessage.sessions[0].events[0].type)
                .describedAs("Event type")
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
        verify(dbInteractor).cleanPostedData(any(), any(), eq(prevReportRequestId + 1), eq(false))
    }

    @Test
    fun onPostRequestCompleteForBadRequest() {
        whenever(responseDataHolder.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
        reportTask.onCreateTask()
        reportTask.onPostRequestComplete(false)
        verify(dbInteractor).cleanPostedData(any(), any(), eq(prevReportRequestId + 1), eq(true))
    }

    @Test
    fun onPostRequestCompleteWithNonBadRequestError() {
        reportTask.onCreateTask()
        reportTask.onPostRequestComplete(false)
        verify(dbInteractor, never()).cleanPostedData(any(), any(), any(), any())
    }

    @Test
    fun onTaskAdded() {
        reportTask.onTaskAdded()
        verify(eventTrigger).disableTrigger()
    }

    @Test
    fun onTaskFinished() {
        clearInvocations(eventTrigger, dbInteractor)
        reportTask.onTaskFinished()
        verifyNoInteractions(eventTrigger, dbInteractor)
    }

    @Test
    fun onTaskRemoved() {
        reportTask.onTaskRemoved()
        verify(eventTrigger).enableTrigger()
        verify(eventTrigger, never()).triggerAsync()
    }

    @Test
    fun onTaskRemovedAfterOnSuccessfulTaskFinished() {
        reportTask.onSuccessfulTaskFinished()
        reportTask.onTaskRemoved()
        verify(eventTrigger).enableTrigger()
        verify(eventTrigger).triggerAsync()
    }

    @Test
    fun onTaskRemovedAfterOnShouldNotExecute() {
        reportTask.onShouldNotExecute()
        reportTask.onTaskRemoved()
        verify(eventTrigger).enableTrigger()
        verify(eventTrigger).triggerAsync()
    }

    @Test
    fun onTaskRemovedAfterShouldNotExecute() {
        reportTask.onShouldNotExecute()
        reportTask.onTaskRemoved()
        verify(eventTrigger).enableTrigger()
        verify(eventTrigger, never()).trigger()
    }

    @Test
    fun onSuccessfulTaskFinished() {
        reportTask.onSuccessfulTaskFinished()
    }

    @Test
    fun onShouldNotExecute() {
        clearInvocations(dbInteractor, eventTrigger)
        reportTask.onShouldNotExecute()
        verifyNoInteractions(dbInteractor, eventTrigger)
    }

    @Test
    fun description() {
        assertThat(reportTask.description()).contains(anonymizedApiKey)
    }

    @Test
    fun eventCountLimitation() {
        val sessionModelsWithLargeAmount = (0 until 100).map { i ->
            DbSessionModel(
                id = i.toLong(),
                type = SessionType.FOREGROUND,
                reportRequestParameters = "",
                description = DbSessionModel.Description(
                    startTime = TimeUtils.currentDeviceTimeSec() + i,
                    serverTimeOffset = 10L * i,
                    obtainedBeforeFirstSynchronization = true,
                )
            )
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
                    .add(
                        DbEventDescriptionToBytesConverter().fromModel(
                            DbEventModel.Description(
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
                            )
                        )
                    ) // FIELD_EVENT_DESCRIPTION
            }
        }
        stubbing(dbInteractor) {
            on { collectAllQueryParameters() } doReturn firstQueryParameter
            on { querySessionModels(any()) } doReturn sessionModelsWithLargeAmount
            on { queryReportsForSessions(any(), any()) } doAnswer {
                // Return all 1000 events under sessionId 0 (the first session in the cursor)
                mapOf(0L to cursorToContentValuesList(reportCursorWithLargeAmountOfEvents))
            }
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

    private fun cursorToContentValuesList(cursor: android.database.MatrixCursor): List<ContentValues> {
        val result = mutableListOf<ContentValues>()
        cursor.moveToPosition(-1)
        while (cursor.moveToNext()) {
            val cv = ContentValues()
            DBUtils.cursorRowToContentValues(cursor, cv)
            result.add(cv)
        }
        return result
    }
}
