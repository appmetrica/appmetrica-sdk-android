package io.appmetrica.analytics.impl.billing

import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor

internal class DummyBillingMonitor : BillingMonitor {

    override fun onSessionResumed() {
        // do nothing
    }

    override fun onBillingConfigChanged(billingConfig: BillingConfig?) {
        // do nothing
    }
}
