package io.appmetrica.analytics.billingv6.internal

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import org.mockito.kotlin.times

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
        verify(uiExecutor, never()).execute(any())
    }

    @Test
    fun onSessionResumedIfNoConfigAfterNullConfigAndDisabledFeature() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, times(1)).execute(any())
    }

    @Test
    fun onSessionResumedIfHasConfig() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onUpdateFinished()
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, times(2)).execute(any())
    }

    @Test
    fun onSessionResumedIfHasConfigAndUnfinishedUpdate() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor).execute(any())
    }

    @Test
    fun onSessionResumedIfNullConfig() {
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())
    }

    @Test
    fun onSessionResumedSequenceOfCalls() {
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())

        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor).execute(any())

        Mockito.clearInvocations(uiExecutor)
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())
    }
}
