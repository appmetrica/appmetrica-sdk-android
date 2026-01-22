package io.appmetrica.analytics.billing.impl

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.billinginterface.internal.monitor.DummyBillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import io.appmetrica.analytics.billingv6.internal.BillingLibraryMonitor as BillingV6LibraryMonitor
import io.appmetrica.analytics.billingv8.internal.BillingLibraryMonitor as BillingV8LibraryMonitor

@RunWith(RobolectricTestRunner::class)
internal class BillingMonitorProviderTest : CommonTest() {

    private val context: Context = mock()
    private val workerExecutor: Executor = mock()
    private val uiExecutor: Executor = mock()
    private val billingInfoStorage: BillingInfoStorage = mock()
    private val billingInfoSender: BillingInfoSender = mock()

    private val provider = BillingMonitorProvider()

    @Test
    fun getV6() {
        val monitor = provider.get(
            context,
            workerExecutor,
            uiExecutor,
            BillingType.LIBRARY_V6,
            billingInfoStorage,
            billingInfoSender
        )

        assertThat(monitor).isInstanceOf(BillingV6LibraryMonitor::class.java)
    }

    @Test
    fun getV8() {
        val monitor = provider.get(
            context,
            workerExecutor,
            uiExecutor,
            BillingType.LIBRARY_V8,
            billingInfoStorage,
            billingInfoSender
        )

        assertThat(monitor).isInstanceOf(BillingV8LibraryMonitor::class.java)
    }

    @Test
    fun getOther() {
        val monitor = provider.get(
            context,
            workerExecutor,
            uiExecutor,
            BillingType.NONE,
            billingInfoStorage,
            billingInfoSender
        )

        assertThat(monitor).isInstanceOf(DummyBillingMonitor::class.java)
    }
}
