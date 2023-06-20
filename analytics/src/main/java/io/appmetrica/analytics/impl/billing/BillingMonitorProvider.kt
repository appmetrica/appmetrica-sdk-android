package io.appmetrica.analytics.impl.billing

import android.content.Context
import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import java.util.concurrent.Executor
import io.appmetrica.analytics.billingv3.internal.BillingLibraryMonitor as BillingV3LibraryMonitor
import io.appmetrica.analytics.billingv4.internal.BillingLibraryMonitor as BillingV4LibraryMonitor

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
            BillingType.LIBRARY_V4 -> {
                YLogger.info(TAG, "Tracking purchases using Billing Library 4")
                BillingV4LibraryMonitor(
                    context,
                    workerExecutor,
                    uiExecutor,
                    billingInfoStorage,
                    billingInfoSender
                )
            }
            BillingType.LIBRARY_V3 -> {
                YLogger.info(TAG, "Tracking purchases using Billing Library 3")
                BillingV3LibraryMonitor(
                    context,
                    workerExecutor,
                    uiExecutor,
                    billingInfoStorage,
                    billingInfoSender
                )
            }
            else -> {
                YLogger.info(TAG, "Do not track purchases")
                DummyBillingMonitor()
            }
        }
    }
}
