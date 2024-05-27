package io.appmetrica.analytics.impl.billing

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor
import io.appmetrica.analytics.billingv6.internal.BillingLibraryMonitor as BillingV6LibraryMonitor

private const val TAG = "[BillingMonitorProvider]"

internal class BillingMonitorProvider {

    operator fun get(
        context: Context,
        workerExecutor: Executor,
        uiExecutor: Executor,
        type: BillingType,
        billingInfoStorage: BillingInfoStorage,
        billingInfoSender: BillingInfoSender
    ): BillingMonitor {
        return when (type) {
            BillingType.LIBRARY_V6 -> {
                DebugLogger.info(TAG, "Tracking purchases using Billing Library 6")
                BillingV6LibraryMonitor(
                    context,
                    workerExecutor,
                    uiExecutor,
                    billingInfoStorage,
                    billingInfoSender
                )
            }
            else -> {
                DebugLogger.info(TAG, "Do not track purchases")
                DummyBillingMonitor()
            }
        }
    }
}
