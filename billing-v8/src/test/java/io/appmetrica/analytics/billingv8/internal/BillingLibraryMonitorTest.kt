package io.appmetrica.analytics.billingv8.internal

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingLibraryMonitorTest : CommonTest() {

    private val context: Context = mock()
    private val uiExecutor = Executor { it.run() }
    private val billingInfoManager: BillingInfoManager = mock()
    private val updatePolicy: UpdatePolicy = mock()
    private val billingInfoStorage: BillingInfoStorage = mock()
    private val billingInfoSender: BillingInfoSender = mock()

    private val billingClientMock = mock<BillingClient>()
    private val billingClientBuilderMock = mock<BillingClient.Builder> {
        on { setListener(any()) } doReturn this.mock
        on { enablePendingPurchases(any()) } doReturn this.mock
        on { build() } doReturn billingClientMock
    }

    @get:Rule
    val billingClientStaticMock = staticRule<BillingClient> {
        on { BillingClient.newBuilder(context) } doReturn billingClientBuilderMock
    }

    private val billingConfig = BillingConfig(41, 42)
    private val workerExecutor: IHandlerExecutor = mock {
        on { post(any()) } doAnswer {
            it.getArgument<Runnable>(0).run()
            null
        }
    }
    private val billingLibraryMonitor = createMonitor(workerExecutor)

    private fun createMonitor(workerExecutor: IHandlerExecutor) = BillingLibraryMonitor(
        context,
        workerExecutor,
        uiExecutor,
        billingInfoStorage,
        billingInfoSender,
        billingInfoManager,
        updatePolicy,
    )

    @Test
    fun onSessionResumedIfNoConfig() {
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock, never()).startConnection(any())
    }

    @Test
    fun onSessionResumedIfNoConfigAfterNullConfigAndDisabledFeature() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock, times(1)).startConnection(any())
        verify(billingClientMock).endConnection()
    }

    @Test
    fun onSessionResumedIfHasConfig() {
        val stateListener = argumentCaptor<BillingClientStateListener>()
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        val calls = inOrder(billingClientMock)
        calls.verify(billingClientMock).startConnection(stateListener.capture())
        val failure = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
            .build()
        stateListener.firstValue.onBillingSetupFinished(failure)
        billingLibraryMonitor.onSessionResumed()
        stateListener.firstValue.onBillingSetupFinished(failure)
        billingLibraryMonitor.onSessionResumed()

        calls.verify(billingClientMock).endConnection()
        calls.verify(billingClientMock).startConnection(any())
        calls.verifyNoMoreInteractions()
        verify(billingClientBuilderMock, times(2)).setListener(any())
        verify(billingClientBuilderMock, times(2)).enablePendingPurchases(any())
    }

    @Test
    fun onSessionResumedIfHasConfigAndUnfinishedUpdate() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock).startConnection(any())
    }

    @Test
    fun `foreground can retry after start connection throws`() {
        doThrow(IllegalStateException("Start connection failed"))
            .doNothing()
            .whenever(billingClientMock)
            .startConnection(any())
        val stateListener = argumentCaptor<BillingClientStateListener>()
        val calls = inOrder(billingClientMock)

        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        calls.verify(billingClientMock).startConnection(stateListener.capture())
        calls.verify(billingClientMock).endConnection()

        billingLibraryMonitor.onSessionResumed()
        calls.verify(billingClientMock).startConnection(any())
        stateListener.firstValue.onBillingSetupFinished(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
                .build()
        )
        billingLibraryMonitor.onSessionResumed()

        calls.verifyNoMoreInteractions()
        verify(billingClientBuilderMock, times(2)).build()
    }

    @Test
    fun `foreground can retry after client construction throws`() {
        whenever(billingClientBuilderMock.build())
            .thenThrow(IllegalStateException("Client construction failed"))
            .thenReturn(billingClientMock)

        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()

        verify(billingClientBuilderMock, times(2)).build()
        verify(billingClientMock).startConnection(any())
    }

    @Test
    fun `foreground waits for queued connection setup callback`() {
        val workerTasks = ArrayDeque<Runnable>()
        val queuedWorker: IHandlerExecutor = mock {
            on { post(any()) } doAnswer {
                workerTasks.addLast(it.getArgument(0))
                null
            }
        }
        val monitor = createMonitor(queuedWorker)
        val setupListener = argumentCaptor<BillingClientStateListener>()
        val failure = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
            .build()

        monitor.onBillingConfigChanged(billingConfig)
        verify(billingClientMock).startConnection(setupListener.capture())
        setupListener.firstValue.onBillingSetupFinished(failure)
        monitor.onSessionResumed()
        verify(billingClientBuilderMock).build()
        verify(billingClientMock, never()).endConnection()

        while (workerTasks.isNotEmpty()) {
            workerTasks.removeFirst().run()
        }
        verify(billingClientBuilderMock, times(2)).build()
        verify(billingClientMock, times(2)).startConnection(any())
        verify(billingClientMock).endConnection()
    }

    @Test
    fun onSessionResumedIfNullConfig() {
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock, never()).startConnection(any())
    }

    @Test
    fun onSessionResumedSequenceOfCalls() {
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock, never()).startConnection(any())

        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock).startConnection(any())

        Mockito.clearInvocations(billingClientMock)
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock, never()).startConnection(any())
    }
}
