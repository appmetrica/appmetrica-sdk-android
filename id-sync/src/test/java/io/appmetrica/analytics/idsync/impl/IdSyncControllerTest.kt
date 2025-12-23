package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrier
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback
import io.appmetrica.analytics.idsync.impl.model.RequestStateHolder
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceStorageProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class IdSyncControllerTest : CommonTest() {

    private val periodicInterval = TimeUnit.MINUTES.toMillis(1)

    private val executor: IHandlerExecutor = mock()

    private val executorProvider: ExecutorProvider = mock {
        on { moduleExecutor } doReturn executor
    }

    private val modulePreferences: ModulePreferences = mock()

    private val serviceStorageProvider: ServiceStorageProvider = mock {
        on { modulePreferences("id-sync") } doReturn modulePreferences
    }

    private val activationBarrier: ActivationBarrier = mock()

    private val serviceContext: ServiceContext = mock {
        on { executorProvider } doReturn executorProvider
        on { serviceStorageProvider } doReturn serviceStorageProvider
        on { activationBarrier } doReturn activationBarrier
    }

    private val sdkIdentifiers: SdkIdentifiers = mock()

    @get:Rule
    val requestStateHolderRule = constructionRule<RequestStateHolder>()
    private val requestStateHolder: RequestStateHolder by requestStateHolderRule

    @get:Rule
    val idSyncRequestControllerRule = constructionRule<IdSyncRequestController>()
    private val idSyncRequestController: IdSyncRequestController by idSyncRequestControllerRule

    private val runnableCaptor = argumentCaptor<Runnable>()
    private val activationBarrierCallbackCaptor = argumentCaptor<ActivationBarrierCallback>()

    private val launchDelaySeconds = 20L
    private val firstRequest: RequestConfig = mock()
    private val secondRequest: RequestConfig = mock()

    private val idSyncConfig: IdSyncConfig = mock {
        on { enabled } doReturn true
        on { launchDelay } doReturn launchDelaySeconds
        on { requests } doReturn listOf(firstRequest, secondRequest)
    }

    private val idSyncController by setUp {
        IdSyncController(serviceContext, sdkIdentifiers)
    }

    @Test
    fun idSyncRequestController() {
        assertThat(idSyncRequestControllerRule.constructionMock.constructed()).hasSize(1)
        assertThat(idSyncRequestControllerRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext, requestStateHolder, sdkIdentifiers)

        assertThat(requestStateHolderRule.constructionMock.constructed()).hasSize(1)
        assertThat(requestStateHolderRule.argumentInterceptor.flatArguments())
            .containsExactly(modulePreferences)
    }

    @Test
    fun `refresh if config is disabled`() {
        whenever(idSyncConfig.enabled).thenReturn(false)
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        verifyNoInteractions(executor, activationBarrier)
    }

    @Test
    fun refreshIfConfigIsEnabled() {
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        verify(activationBarrier).subscribe(
            eq(launchDelaySeconds),
            eq(executor),
            activationBarrierCallbackCaptor.capture()
        )
        verify(executor, never()).executeDelayed(runnableCaptor.capture(), eq(periodicInterval))

        activationBarrierCallbackCaptor.firstValue.onWaitFinished()
        verify(idSyncRequestController).handle(firstRequest)
        verify(idSyncRequestController).handle(secondRequest)

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(periodicInterval))
    }

    @Test
    fun `refresh with same config with enabled twice`() {
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        clearInvocations(activationBarrier, executor)
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)

        verifyNoInteractions(activationBarrier, executor)
    }

    @Test
    fun `refresh with enabled twice`() {
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        clearInvocations(activationBarrier, executor)

        val config = mock<IdSyncConfig> {
            on { enabled } doReturn true
            on { requests } doReturn listOf(firstRequest, secondRequest)
            on { launchDelay } doReturn launchDelaySeconds
        }

        idSyncController.refresh(config, sdkIdentifiers)
        verifyNoInteractions(activationBarrier, executor)
    }

    @Test
    fun `refresh with disabled after enabled`() {
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        verify(activationBarrier).subscribe(
            eq(launchDelaySeconds),
            eq(executor),
            activationBarrierCallbackCaptor.capture()
        )
        clearInvocations(activationBarrier, executor)
        idSyncController.refresh(
            mock<IdSyncConfig> {
                on { enabled } doReturn false
                on { requests } doReturn listOf(firstRequest, secondRequest)
                on { launchDelay } doReturn launchDelaySeconds
            },
            sdkIdentifiers
        )
        verify(executor).remove(runnableCaptor.capture())
        verifyNoInteractions(activationBarrier)
        clearInvocations(idSyncRequestController, executor, activationBarrier)

        activationBarrierCallbackCaptor.firstValue.onWaitFinished()
        verifyNoInteractions(idSyncRequestController, executor, activationBarrier)
    }

    @Test
    fun `refresh with disabled twice after enabled`() {
        idSyncController.refresh(idSyncConfig, sdkIdentifiers)
        whenever(idSyncConfig.enabled).thenReturn(false)
        idSyncController.refresh(
            mock<IdSyncConfig> {
                on { enabled } doReturn false
                on { requests } doReturn listOf(firstRequest, secondRequest)
                on { launchDelay } doReturn launchDelaySeconds
            },
            sdkIdentifiers
        )
        clearInvocations(activationBarrier, executor, idSyncRequestController)

        idSyncController.refresh(
            mock<IdSyncConfig> {
                on { enabled } doReturn false
                on { requests } doReturn listOf(firstRequest, secondRequest)
                on { launchDelay } doReturn launchDelaySeconds
            },
            sdkIdentifiers
        )

        verifyNoInteractions(executor, activationBarrier)
    }
}
