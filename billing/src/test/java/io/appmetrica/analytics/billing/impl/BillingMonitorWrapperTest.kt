package io.appmetrica.analytics.billing.impl

import android.content.Context
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideBillingConfig
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.billing.impl.sender.BillingInfoSenderImpl
import io.appmetrica.analytics.billing.impl.storage.AutoInappCollectingInfo
import io.appmetrica.analytics.billing.impl.storage.BillingInfoStorageImpl
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleConfig
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleReporter
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentContext
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceStorageProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BillingMonitorWrapperTest : CommonTest() {

    private val context: Context = mock()
    private val defaultExecutor: IHandlerExecutor = mock()
    private val uiExecutor: IHandlerExecutor = mock()
    private val reportRunnableExecutor: IHandlerExecutor = mock()
    private val executorProvider: ExecutorProvider = mock {
        on { getDefaultExecutor() } doReturn defaultExecutor
        on { getUiExecutor() } doReturn uiExecutor
        on { getReportRunnableExecutor() } doReturn reportRunnableExecutor
    }
    private val applicationStateProvider: ApplicationStateProvider = mock()
    private val serviceStorageProvider: ServiceStorageProvider = mock {
        on {
            createBinaryStateStorageFactory(
                any(),
                any<ProtobufStateSerializer<AutoInappCollectingInfoProto.AutoInappCollectingInfo>>(),
                any<ProtobufConverter<AutoInappCollectingInfo, AutoInappCollectingInfoProto.AutoInappCollectingInfo>>()
            )
        } doReturn mock()
    }
    private val serviceContext: ServiceContext = mock {
        on { context } doReturn context
        on { executorProvider } doReturn executorProvider
        on { applicationStateProvider } doReturn applicationStateProvider
        on { serviceStorageProvider } doReturn serviceStorageProvider
    }

    private val serviceComponentModuleReporter: ServiceComponentModuleReporter = mock()
    private val serviceComponentModuleConfig: ServiceComponentModuleConfig = mock {
        on { isRevenueAutoTrackingEnabled() } doReturn true
    }
    private val serviceModuleReporterComponentContext: ServiceModuleReporterComponentContext = mock {
        on { reporter } doReturn serviceComponentModuleReporter
        on { config } doReturn serviceComponentModuleConfig
    }

    private val serviceSideRemoteBillingConfig = ServiceSideRemoteBillingConfig(
        enabled = true,
        config = ServiceSideBillingConfig(
            sendFrequencySeconds = 42,
            firstCollectingInappMaxAgeSeconds = 4242
        )
    )

    @get:Rule
    val billingTypeDetectorRule = staticRule<BillingTypeDetector> {
        on { BillingTypeDetector.getBillingType() } doReturn BillingType.LIBRARY_V8
    }

    @get:Rule
    val billingInfoStorageImplRule = constructionRule<BillingInfoStorageImpl>()

    @get:Rule
    val billingInfoSenderImplRule = constructionRule<BillingInfoSenderImpl>()

    private val billingMonitor: BillingMonitor = mock()

    @get:Rule
    val billingMonitorProviderRule = constructionRule<BillingMonitorProvider> {
        on {
            get(
                eq(context),
                eq(defaultExecutor),
                eq(uiExecutor),
                eq(BillingType.LIBRARY_V8),
                any(),
                any()
            )
        } doReturn billingMonitor
    }

    private val wrapper = BillingMonitorWrapper(
        serviceContext,
        serviceSideRemoteBillingConfig
    )

    @Test
    fun onMainReporterCreated() {
        whenever(applicationStateProvider.registerStickyObserver(any())).thenReturn(ApplicationState.VISIBLE)

        wrapper.onMainReporterCreated(serviceModuleReporterComponentContext)

        assertThat(billingMonitorProviderRule.constructionMock.constructed()).isNotEmpty()
        verify(billingMonitor).onBillingConfigChanged(BillingConfig(42, 4242))
        verify(applicationStateProvider).registerStickyObserver(any())
        verify(billingMonitor).onSessionResumed()
    }

    @Test
    fun onMainReporterCreatedIfNotVisible() {
        whenever(applicationStateProvider.registerStickyObserver(any())).thenReturn(ApplicationState.BACKGROUND)

        wrapper.onMainReporterCreated(serviceModuleReporterComponentContext)

        assertThat(billingMonitorProviderRule.constructionMock.constructed()).isNotEmpty()
        verify(billingMonitor).onBillingConfigChanged(BillingConfig(42, 4242))
        verify(applicationStateProvider).registerStickyObserver(any())
        verify(billingMonitor, never()).onSessionResumed()
    }

    @Test
    fun onMainReporterCreatedIfNoConfig() {
        whenever(applicationStateProvider.registerStickyObserver(any())).thenReturn(ApplicationState.VISIBLE)
        val wrapper = BillingMonitorWrapper(serviceContext, null)

        wrapper.onMainReporterCreated(serviceModuleReporterComponentContext)

        assertThat(billingMonitorProviderRule.constructionMock.constructed()).isNotEmpty()
        verify(billingMonitor, never()).onBillingConfigChanged(any())
        verify(applicationStateProvider).registerStickyObserver(any())
        verify(billingMonitor).onSessionResumed()
    }

    @Test
    fun updateConfig() {
        whenever(applicationStateProvider.registerStickyObserver(any())).thenReturn(ApplicationState.VISIBLE)

        wrapper.onMainReporterCreated(serviceModuleReporterComponentContext)

        assertThat(billingMonitorProviderRule.constructionMock.constructed()).isNotEmpty()
        verify(billingMonitor).onBillingConfigChanged(BillingConfig(42, 4242))
        verify(applicationStateProvider).registerStickyObserver(any())
        verify(billingMonitor).onSessionResumed()

        val newServiceSideRemoteBillingConfig = ServiceSideRemoteBillingConfig(
            enabled = true,
            config = ServiceSideBillingConfig(
                sendFrequencySeconds = 43,
                firstCollectingInappMaxAgeSeconds = 4343
            )
        )
        wrapper.updateConfig(newServiceSideRemoteBillingConfig)

        verify(billingMonitor).onBillingConfigChanged(BillingConfig(43, 4343))
    }

    @Test
    fun onMainReporterCreatedIfRevenueAutoTrackingIsDisabled() {
        whenever(serviceComponentModuleConfig.isRevenueAutoTrackingEnabled()).thenReturn(false)
        wrapper.onMainReporterCreated(serviceModuleReporterComponentContext)

        assertThat(billingMonitorProviderRule.constructionMock.constructed()).isEmpty()
    }
}
