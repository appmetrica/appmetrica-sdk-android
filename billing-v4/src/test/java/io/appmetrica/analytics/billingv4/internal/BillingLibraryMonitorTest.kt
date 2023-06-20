package io.appmetrica.analytics.billingv4.internal

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
class BillingLibraryMonitorTest {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var workerExecutor: Executor
    @Mock
    private lateinit var uiExecutor: Executor
    @Mock
    private lateinit var billingInfoManager: BillingInfoManager
    @Mock
    private lateinit var updatePolicy: UpdatePolicy
    @Mock
    private lateinit var billingInfoStorage: BillingInfoStorage
    @Mock
    private lateinit var billingInfoSender: BillingInfoSender

    private lateinit var billingLibraryMonitor: BillingLibraryMonitor

    private val billingConfig = BillingConfig(41, 42)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.`when`(workerExecutor).execute(any<SafeRunnable>())
        Mockito.doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.`when`(uiExecutor).execute(any<SafeRunnable>())
        billingLibraryMonitor = BillingLibraryMonitor(
            context,
            workerExecutor,
            uiExecutor,
            billingInfoStorage,
            billingInfoSender,
            billingInfoManager,
            updatePolicy
        )
    }

    @Test
    fun testOnSessionResumedIfNoConfig() {
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())
    }

    @Test
    fun testOnSessionResumedIfNoConfigAfterNullConfigAndDisabledFeature() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())
    }

    @Test
    fun testOnSessionResumedIfHasConfig() {
        billingLibraryMonitor.onBillingConfigChanged(billingConfig)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor).execute(any())
    }

    @Test
    fun testOnSessionResumedIfNullConfig() {
        billingLibraryMonitor.onBillingConfigChanged(null)
        billingLibraryMonitor.onSessionResumed()
        verify(uiExecutor, never()).execute(any())
    }

    @Test
    fun testOnSessionResumedSequenceOfCalls() {
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

    @Test
    fun testGetBillingInfoManager() {
        assertThat(billingLibraryMonitor.billingInfoManager).isEqualTo(billingInfoManager)
    }

    @Test
    fun testGetUpdatePolicy() {
        assertThat(billingLibraryMonitor.updatePolicy).isEqualTo(updatePolicy)
    }

    @Test
    fun testGetBillingInfoSender() {
        assertThat(billingLibraryMonitor.billingInfoSender).isEqualTo(billingInfoSender)
    }

    @Test
    fun testGetUiExecutor() {
        assertThat(billingLibraryMonitor.uiExecutor).isEqualTo(uiExecutor)
    }

    @Test
    fun testGetWorkerExecutor() {
        assertThat(billingLibraryMonitor.workerExecutor).isEqualTo(workerExecutor)
    }
}
