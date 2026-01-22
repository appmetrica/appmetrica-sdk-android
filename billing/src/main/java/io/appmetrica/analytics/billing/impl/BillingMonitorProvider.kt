package io.appmetrica.analytics.billing.impl

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.billinginterface.internal.monitor.DummyBillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor
import io.appmetrica.analytics.billingv6.internal.BillingLibraryMonitor as BillingV6LibraryMonitor
import io.appmetrica.analytics.billingv8.internal.BillingLibraryMonitor as BillingV8LibraryMonitor

internal class BillingMonitorProvider {

    private val tag = "[BillingMonitorProvider]"

    fun get(
        context: Context,
        workerExecutor: Executor,
        uiExecutor: Executor,
        type: BillingType,
        billingInfoStorage: BillingInfoStorage,
        billingInfoSender: BillingInfoSender
    ): BillingMonitor {
        DebugLogger.info(tag, "Billing type: $type")
        return when (type) {
            BillingType.LIBRARY_V6 -> {
                DebugLogger.info(tag, "Tracking purchases using Billing Library 6")
                BillingV6LibraryMonitor(
                    context,
                    workerExecutor,
                    uiExecutor,
                    billingInfoStorage,
                    billingInfoSender
                )
            }

            BillingType.LIBRARY_V8 -> {
                DebugLogger.info(tag, "Tracking purchases using Billing Library 8")
                BillingV8LibraryMonitor(
                    context,
                    workerExecutor,
                    uiExecutor,
                    billingInfoStorage,
                    billingInfoSender
                )
            }

            else -> {
                DebugLogger.info(tag, "Do not track purchases")
                DummyBillingMonitor()
            }
        }
    }
}
