package io.appmetrica.analytics.billingv8.internal

import android.content.Context
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingLibraryMonitorTest : CommonTest() {

    private val context: Context = mock()
    private val workerExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
    private val uiExecutor: Executor = mock {
        on { execute(any<SafeRunnable>()) } doAnswer {
            (it.getArgument<Any>(0) as Runnable).run()
            null
        }
    }
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
    private val billingLibraryMonitor = BillingLibraryMonitor(
        context,
        workerExecutor,
        uiExecutor,
        billingInfoStorage,
        billingInfoSender,
        billingInfoManager,
        updatePolicy
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
    }

    @Test
    fun onSessionResumedIfHasConfig() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onUpdateFinished()
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientBuilderMock, times(2)).setListener(any())
        verify(billingClientBuilderMock, times(2)).enablePendingPurchases(any())
        verify(billingClientMock, times(2)).startConnection(any())
    }

    @Test
    fun onSessionResumedIfHasConfigAndUnfinishedUpdate() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(billingClientMock).startConnection(any())
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
