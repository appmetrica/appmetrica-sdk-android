package io.appmetrica.analytics.idsync.impl

import android.content.Context
import android.net.Uri
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceNetworkContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
internal class IdSyncResultRealtimeReporterTest : CommonTest() {

    private val context: Context = mock()
    private val uuid = UUID.randomUUID().toString()
    private val deviceId = UUID.randomUUID().toString()
    private val appSetIdValue = UUID.randomUUID().toString()
    private val googleAdvId = UUID.randomUUID().toString()
    private val huaweiAdvId = UUID.randomUUID().toString()
    private val yandexAdvId = UUID.randomUUID().toString()

    private val sdkIdentifiers: SdkIdentifiers = mock {
        on { uuid } doReturn uuid
        on { deviceId } doReturn deviceId
    }

    private val google = AdTrackingInfoResult(
        AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleAdvId, true),
        IdentifierStatus.OK,
        null
    )

    private val huawei = AdTrackingInfoResult(
        AdTrackingInfo(AdTrackingInfo.Provider.HMS, huaweiAdvId, true),
        IdentifierStatus.OK,
        null
    )

    private val yandex = AdTrackingInfoResult(
        AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, true),
        IdentifierStatus.OK,
        null
    )

    private val advIdentifiersHolder: AdvertisingIdsHolder = mock {
        on { google } doReturn google
        on { huawei } doReturn huawei
        on { yandex } doReturn yandex
    }

    private val advIdentifiersGetter: AdvertisingIdsProvider = mock {
        on { identifiers } doReturn advIdentifiersHolder
    }

    private val appSetIdProvider: AppSetIdProvider = mock {
        on { getAppSetId() } doReturn AppSetId(appSetIdValue, AppSetIdScope.APP)
    }

    private val platformIdentifiers: PlatformIdentifiers = mock {
        on { advIdentifiersProvider } doReturn advIdentifiersGetter
        on { appSetIdProvider } doReturn appSetIdProvider
    }

    private val sslSocketFactory: SSLSocketFactory = mock()
    private val sslSocketFactoryProvider: SslSocketFactoryProvider = mock {
        on { sslSocketFactory } doReturn sslSocketFactory
    }

    private val networkContext: ServiceNetworkContext = mock {
        on { sslSocketFactoryProvider } doReturn sslSocketFactoryProvider
    }

    private val executor: IHandlerExecutor = mock()

    private val executorProvider: ExecutorProvider = mock {
        on { getSupportIOExecutor() } doReturn executor
    }

    private val serviceContext: ServiceContext = mock {
        on { context } doReturn context
        on { platformIdentifiers } doReturn platformIdentifiers
        on { executorProvider } doReturn executorProvider
        on { networkContext } doReturn networkContext
    }

    private val reportingUrl = Uri.Builder()
        .scheme("https")
        .authority("example.com")
        .path("/report")
        .build()
        .toString()

    private val reportingResult = "Some reporting result value"

    private val now = 100500L

    @get:Rule
    val timeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn now
    }
    private val timeProvider by timeProviderRule

    @get:Rule
    val idSyncResultRequestSenderRule = constructionRule<IdSyncResultRequestSender>()
    private val idSyncResultRequestSender by idSyncResultRequestSenderRule

    private val urlArgumentCaptor = argumentCaptor<String>()
    private val valueArgumentCaptor = argumentCaptor<String>()
    private val runnableCaptor = argumentCaptor<Runnable>()

    private val reporter by setUp { IdSyncResultRealtimeReporter(serviceContext, reportingUrl) }

    @Test
    fun requestSender() {
        assertThat(idSyncResultRequestSenderRule.constructionMock.constructed()).hasSize(1)
        assertThat(idSyncResultRequestSenderRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)
    }

    @Test
    fun `send request - check uuid`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("uuid=$uuid")
    }

    @Test
    fun `send request - check deviceId`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("deviceid=$deviceId")
    }

    @Test
    fun `send request - check google advId`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("adv_id=$googleAdvId")
    }

    @Test
    fun `send request - check huawei oaid`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("oaid=$huaweiAdvId")
    }

    @Test
    fun `send request - check yandex advId`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("yandex_adv_id=$yandexAdvId")
    }

    @Test
    fun `send request - check appSetId`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).contains("app_set_id=$appSetIdValue")
    }

    @Test
    fun `send request - url does not contain google advId when null`() {
        whenever(advIdentifiersHolder.google)
            .thenReturn(AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, null))
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).doesNotContainPattern("([?&])adv_id=")
    }

    @Test
    fun `send request - url does not contain huawei oaid when null`() {
        whenever(advIdentifiersHolder.huawei)
            .thenReturn(AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, null))
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).doesNotContainPattern("([?&])oaid=")
    }

    @Test
    fun `send request - url does not contain yandex advId when null`() {
        whenever(advIdentifiersHolder.yandex)
            .thenReturn(AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, null))
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).doesNotContainPattern("([?&])yandex_adv_id=")
    }

    @Test
    fun `send request - url does not contain appSetId when null`() {
        whenever(appSetIdProvider.getAppSetId())
            .thenReturn(AppSetId(null, AppSetIdScope.APP))
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(urlArgumentCaptor.firstValue).doesNotContainPattern("([?&])app_set_id=")
    }

    @Test
    fun `send request - check value`() {
        reporter.reportResult(reportingResult, sdkIdentifiers)
        interceptRequest()
        assertThat(valueArgumentCaptor.firstValue).isEqualTo(reportingResult)
    }

    @Test
    fun `send request - check retries`() {
        // Fail two first attempts
        whenever(idSyncResultRequestSender.sendRequest(any(), any()))
            .thenReturn(false, false, true)
        reporter.reportResult(reportingResult, sdkIdentifiers)

        verify(executor).execute(runnableCaptor.capture())

        clearInvocations(executor, idSyncResultRequestSender)
        runnableCaptor.lastValue.run()
        verify(idSyncResultRequestSender).sendRequest(any(), any())

        // First retry
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(1000L))
        verifyNoMoreInteractions(executor)
        clearInvocations(executor)
        runnableCaptor.lastValue.run()
        verify(idSyncResultRequestSender, times(2)).sendRequest(any(), any())

        // Second retry
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(2000L))
        verifyNoMoreInteractions(executor)
        clearInvocations(executor)
        runnableCaptor.lastValue.run()
        verify(idSyncResultRequestSender, times(3)).sendRequest(any(), any())

        verifyNoInteractions(executor)
    }

    @Test
    fun `send request - check max retry times`() {
        // Fail all attempts
        whenever(idSyncResultRequestSender.sendRequest(any(), any()))
            .thenReturn(false)

        whenever(executor.execute(any()))
            .thenAnswer { (it.arguments[0] as Runnable).run() }
        whenever(executor.executeDelayed(any(), any()))
            .thenAnswer {
                (it.arguments[0] as Runnable).run()
                whenever(timeProvider.currentTimeMillis()).thenReturn(now + it.arguments[1] as Long)
            }

        reporter.reportResult(reportingResult, sdkIdentifiers)

        verify(executor, times(5)).executeDelayed(any(), any())
        verify(idSyncResultRequestSender, times(6)).sendRequest(any(), any())
    }

    private fun interceptRequest() {
        verify(executor).execute(runnableCaptor.capture())
        verifyNoInteractions(idSyncResultRequestSender)
        runnableCaptor.firstValue.run()
        verify(idSyncResultRequestSender)
            .sendRequest(urlArgumentCaptor.capture(), valueArgumentCaptor.capture())
    }
}
