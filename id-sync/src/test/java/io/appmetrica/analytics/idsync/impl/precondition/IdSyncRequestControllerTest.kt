package io.appmetrica.analytics.idsync.impl.precondition

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.idsync.impl.IdSyncRequestController
import io.appmetrica.analytics.idsync.impl.IdSyncRequestSender
import io.appmetrica.analytics.idsync.impl.IdSyncResultHandler
import io.appmetrica.analytics.idsync.impl.RequestResult
import io.appmetrica.analytics.idsync.impl.model.RequestAttemptResult
import io.appmetrica.analytics.idsync.impl.model.RequestState
import io.appmetrica.analytics.idsync.impl.model.RequestStateHolder
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceNetworkContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class IdSyncRequestControllerTest : CommonTest() {

    private val sslSocketFactoryProvider: SslSocketFactoryProvider = mock()

    private val networkContext: ServiceNetworkContext = mock {
        on { sslSocketFactoryProvider } doReturn sslSocketFactoryProvider
    }

    private val ioExecutor: IHandlerExecutor = mock()
    private val moduleExecutor: IHandlerExecutor = mock()

    private val executorProvider: ExecutorProvider = mock {
        on { getSupportIOExecutor() } doReturn ioExecutor
        on { moduleExecutor } doReturn moduleExecutor
    }

    private val serviceContext: ServiceContext = mock {
        on { networkContext } doReturn networkContext
        on { executorProvider } doReturn executorProvider
    }

    private val requestStateHolder: RequestStateHolder = mock()
    private val sdkIdentifiers: SdkIdentifiers = mock()

    private val now = System.currentTimeMillis()

    @get:Rule
    val timeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn now
    }
    private val timeProvider: SystemTimeProvider by timeProviderRule

    @get:Rule
    val idSyncRequestSenderRule = constructionRule<IdSyncRequestSender>()
    private val idSyncRequestSender: IdSyncRequestSender by idSyncRequestSenderRule

    @get:Rule
    val eventReporterRule = constructionRule<IdSyncResultHandler>()
    private val eventReporter: IdSyncResultHandler by eventReporterRule

    private val preconditions: Preconditions = mock()

    private val resendIntervalForInvalidResponse = 100500L
    private val resendIntervalForValidResponse = 200500L

    private val requestType = "some request type"
    private val requestConfig: RequestConfig = mock {
        on { type } doReturn requestType
        on { url } doReturn "url"
        on { validResponseCodes } doReturn listOf(200)
        on { preconditions } doReturn preconditions
        on { resendIntervalForValidResponse } doReturn resendIntervalForValidResponse
        on { resendIntervalForInvalidResponse } doReturn resendIntervalForInvalidResponse
    }

    private val preconditionVerifier: PreconditionVerifier = mock {
        on { matchPrecondition() } doReturn true
    }

    @get:Rule
    val preconditionProviderRule = constructionRule<PreconditionProvider> {
        on { getPrecondition(preconditions) } doReturn preconditionVerifier
    }
    private val preconditionProvider: PreconditionProvider by preconditionProviderRule

    private val runnableCaptor = argumentCaptor<Runnable>()

    private val requestState: RequestState = mock()

    private val requestResultResponseCode = 200
    private val requestResult: RequestResult = mock {
        on { isCompleted } doReturn true
        on { type } doReturn requestType
        on { responseCode } doReturn requestResultResponseCode
        on { responseCodeIsValid } doReturn true
    }

    private val requestStateCaptor = argumentCaptor<RequestState>()
    private val controller by setUp { IdSyncRequestController(serviceContext, requestStateHolder, sdkIdentifiers) }

    @Test
    fun requestSender() {
        assertThat(idSyncRequestSenderRule.constructionMock.constructed()).hasSize(1)
        assertThat(idSyncRequestSenderRule.argumentInterceptor.flatArguments())
            .containsExactly(sslSocketFactoryProvider, controller)
    }

    @Test
    fun preconditionProvider() {
        assertThat(preconditionProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(preconditionProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)
    }

    @Test
    fun eventReporter() {
        assertThat(eventReporterRule.constructionMock.constructed()).hasSize(1)
        assertThat(eventReporterRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)
    }

    @Test
    fun `handle for first time`() {
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        verifyNoInteractions(idSyncRequestSender)
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
    }

    @Test
    fun `handle for first time if empty type`() {
        whenever(requestConfig.type).thenReturn("")
        controller.handle(requestConfig)
        verifyNoInteractions(idSyncRequestSender, ioExecutor)
    }

    @Test
    fun `handle for first time if empty url`() {
        whenever(requestConfig.url).thenReturn("")
        controller.handle(requestConfig)
        verifyNoInteractions(idSyncRequestSender, ioExecutor)
    }

    @Test
    fun `handle for first time if empty validResponseCodes`() {
        whenever(requestConfig.validResponseCodes).thenReturn(listOf())
        controller.handle(requestConfig)
        verifyNoInteractions(idSyncRequestSender, ioExecutor)
    }

    @Test
    fun `handle for first time if preconditions does not match`() {
        whenever(preconditionVerifier.matchPrecondition()).thenReturn(false)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verifyNoInteractions(idSyncRequestSender)
    }

    @Test
    fun `handle for second time if prev is success and send long ago`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.SUCCESS)
        whenever(requestState.lastAttempt).thenReturn(now - resendIntervalForValidResponse - 1)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
    }

    @Test
    fun `handle for second time if prev is success and send recently`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.SUCCESS)
        whenever(requestState.lastAttempt).thenReturn(now - resendIntervalForValidResponse + 1)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        controller.handle(requestConfig)
        verifyNoInteractions(idSyncRequestSender, ioExecutor)
    }

    @Test
    fun `handle for second time if prev is failed and send long ago`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.FAILURE)
        whenever(requestState.lastAttempt).thenReturn(now - resendIntervalForInvalidResponse - 1)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
    }

    @Test
    fun `handle for second time if prev is failed and send recently`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.FAILURE)
        whenever(requestState.lastAttempt).thenReturn(now - resendIntervalForInvalidResponse + 1)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        controller.handle(requestConfig)
        verifyNoInteractions(idSyncRequestSender, ioExecutor)
    }

    @Test
    fun `handle for second time if prev state is incompatible`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.INCOMPATIBLE_PRECONDITION)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        whenever(requestState.lastAttempt).thenReturn(now)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
    }

    @Test
    fun `handle for second time if prev state is failure`() {
        whenever(requestState.lastAttemptResult).thenReturn(RequestAttemptResult.FAILURE)
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(requestState)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
        verifyNoInteractions(eventReporter)
    }

    @Test
    fun `handle for second time if prev state is null`() {
        whenever(requestStateHolder.getRequestState(requestConfig.type)).doReturn(null)
        controller.handle(requestConfig)
        verify(ioExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(idSyncRequestSender).sendRequest(requestConfig)
        verifyNoInteractions(eventReporter)
    }

    @Test
    fun `onResult if incomplete`() {
        whenever(requestResult.isCompleted).thenReturn(false)
        controller.onResult(requestResult, requestConfig)
        verify(moduleExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verifyNoInteractions(eventReporter, requestStateHolder)
    }

    @Test
    fun `onResult if complete and has valid response code`() {
        controller.onResult(requestResult, requestConfig)

        verifyNoInteractions(eventReporter, requestStateHolder)
        verify(moduleExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(eventReporter).reportEvent(requestResult, requestConfig, sdkIdentifiers)
        verify(requestStateHolder).updateRequestState(requestStateCaptor.capture())

        ObjectPropertyAssertions(requestStateCaptor.firstValue)
            .checkField("type", requestType)
            .checkField("lastAttempt", now)
            .checkField("lastAttemptResult", RequestAttemptResult.SUCCESS)
            .checkAll()
    }

    @Test
    fun `onResult if complete and has invalid response code`() {
        whenever(requestResult.responseCodeIsValid).thenReturn(false)
        controller.onResult(requestResult, requestConfig)
        verifyNoInteractions(eventReporter, requestStateHolder)
        verify(moduleExecutor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(eventReporter).reportEvent(requestResult, requestConfig, sdkIdentifiers)
        verify(requestStateHolder).updateRequestState(requestStateCaptor.capture())
        ObjectPropertyAssertions(requestStateCaptor.firstValue)
            .checkField("type", requestType)
            .checkField("lastAttempt", now)
            .checkField("lastAttemptResult", RequestAttemptResult.FAILURE)
            .checkAll()
    }
}
