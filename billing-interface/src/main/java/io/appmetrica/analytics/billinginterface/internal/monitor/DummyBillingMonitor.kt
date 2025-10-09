package io.appmetrica.analytics.billinginterface.internal.monitor

import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig

class DummyBillingMonitor : BillingMonitor {

    override fun onSessionResumed() {
        // do nothing
    }

    override fun onBillingConfigChanged(billingConfig: BillingConfig?) {
        // do nothing
    }
}
