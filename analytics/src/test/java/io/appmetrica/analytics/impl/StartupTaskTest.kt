package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.network.Constants
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.request.appenders.StartupParamsAppender
import io.appmetrica.analytics.impl.startup.StartupError
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.parsing.StartupResult
import io.appmetrica.analytics.networktasks.internal.ConfigProvider
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class StartupTaskTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val startupResult = mock<StartupResult>()

    private val responseHeaders = mock<Map<String, List<String>>>()

    private val responseDataHolder = mock<ResponseDataHolder> {
        on { responseHeaders } doReturn responseHeaders
    }

    private val requestDataHolder = mock<RequestDataHolder>()

    val startupHost = "some.startup.host"
    val startupHosts = listOf(startupHost)

    val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val startupRequestConfig = mock<StartupRequestConfig> {
        on { startupHosts } doReturn startupHosts
        on { retryPolicyConfig } doReturn retryPolicyConfig
    }

    private val configProvider = mock<ConfigProvider<StartupRequestConfig>> {
        on { config } doReturn startupRequestConfig
    }

    private val packageName = "fake.package"
    private val componentIdToString = "componentId#toString"
    private val componentId = mock<ComponentId> {
        on { `package` } doReturn packageName
        on { toString() } doReturn componentIdToString
    }

    private val startupUnit = mock<StartupUnit> {
        on { componentId } doReturn componentId
    }

    private val startupParamsAppender = mock<StartupParamsAppender>()

    private val responseHandler = mock<StartupNetworkResponseHandler> {
        on { handle(responseDataHolder) } doReturn startupResult
    }

    private val fullUrlFormer = mock<FullUrlFormer<StartupRequestConfig>>()

    @get:Rule
    val startupNetworkResponseHandlerRule =
        MockedConstructionRule(StartupNetworkResponseHandler::class.java)

    private lateinit var startupTask: StartupTask

    @Before
    fun setUp() {
        startupTask = StartupTask(
            startupUnit,
            responseHandler,
            fullUrlFormer,
            requestDataHolder,
            responseDataHolder,
            configProvider
        )
    }

    @Test
    fun setHostsOnConstructor() {
        verify(fullUrlFormer).setHosts(startupHosts)
    }

    @Test
    fun onCreateTaskIfRequired() = onCreateTask(true)

    @Test
    fun onCreateTaskIfNotRequired() = onCreateTask(false)

    private fun onCreateTask(startupRequired: Boolean) {
        whenever(startupUnit.isStartupRequired()).thenReturn(startupRequired)
        assertThat(startupTask.onCreateTask()).isEqualTo(startupRequired)
        verify(requestDataHolder)
            .setHeader(Constants.Headers.ACCEPT_ENCODING, Constants.Config.ENCODING_ENCRYPTED)
    }

    @Test
    fun onRequestComplete() {
        assertThat(startupTask.onRequestComplete()).isTrue()
    }

    @Test
    fun onRequestCompleteForNullResult() {
        whenever(responseHandler.handle(responseDataHolder)).thenReturn(null)
        assertThat(startupTask.onRequestComplete()).isFalse()
    }

    @Test
    fun onUnsuccessfulTaskFinished() {
        startupTask.onUnsuccessfulTaskFinished()
        verify(startupUnit).onRequestError(StartupError.UNKNOWN)
    }

    @Test
    fun onUnsuccessfulTaskFinishedAfterShouldNotExecute() {
        startupTask.onShouldNotExecute()
        startupTask.onUnsuccessfulTaskFinished()
        verify(startupUnit).onRequestError(StartupError.NETWORK)
    }

    @Test
    fun onUnsuccessfulTaskFinishedAfterRequestError() {
        startupTask.onRequestError(IOException())
        startupTask.onUnsuccessfulTaskFinished()
        verify(startupUnit).onRequestError(StartupError.NETWORK)
    }

    @Test
    fun onUnsuccessfulTaskFinishedAfterOnPostRequestComplete() {
        startupTask.onPostRequestComplete(true)
        startupTask.onUnsuccessfulTaskFinished()
        verify(startupUnit).onRequestError(StartupError.UNKNOWN)
    }

    @Test
    fun onUnsuccessfulTaskFinishedAfterFailedOnPostRequestComplete() {
        startupTask.onPostRequestComplete(false)
        startupTask.onUnsuccessfulTaskFinished()
        verify(startupUnit).onRequestError(StartupError.PARSE)
    }

    @Test
    fun onSuccessfulTaskFinished() {
        startupTask.onRequestComplete()
        startupTask.onSuccessfulTaskFinished()
        verify(startupUnit).onRequestComplete(startupResult, startupRequestConfig, responseHeaders)
    }

    @Test
    fun onSuccessfulTaskFinishedIfResultIsNull() {
        whenever(responseHandler.handle(responseDataHolder)).thenReturn(null)
        startupTask.onRequestComplete()
        startupTask.onSuccessfulTaskFinished()
        verify(startupUnit, never()).onRequestComplete(any(), any(), any())
    }

    @Test
    fun onSuccessfulTaskFinishedIfResponseHeadersIsNull() {
        whenever(responseDataHolder.responseHeaders).thenReturn(null)
        startupTask.onRequestComplete()
        startupTask.onSuccessfulTaskFinished()
        verify(startupUnit, never()).onRequestComplete(any(), any(), any())
    }

    @Test
    fun description() {
        assertThat(startupTask.description()).contains(componentIdToString)
    }

    @Test
    fun getRetryPolicyConfig() {
        assertThat(startupTask.retryPolicyConfig).isEqualTo(retryPolicyConfig)
    }

    @Test
    fun getRequestDataHolder() {
        assertThat(startupTask.requestDataHolder).isEqualTo(requestDataHolder)
    }

    @Test
    fun getResponseDataHolder() {
        assertThat(startupTask.responseDataHolder).isEqualTo(responseDataHolder)
    }

    @Test
    fun getFullUrlFormer() {
        assertThat(startupTask.fullUrlFormer).isEqualTo(fullUrlFormer)
    }

    @Test
    fun getSslSocketFactory() {
        assertThat(startupTask.sslSocketFactory)
            .isEqualTo(GlobalServiceLocator.getInstance().sslSocketFactoryProvider.sslSocketFactory)
    }
}
