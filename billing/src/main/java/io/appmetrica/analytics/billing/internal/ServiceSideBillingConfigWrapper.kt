package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig

class ServiceSideBillingConfigWrapper internal constructor(
    internal val config: ServiceSideRemoteBillingConfig
) {
    companion object {
        internal fun ServiceSideRemoteBillingConfig.toWrapper() = ServiceSideBillingConfigWrapper(this)
    }
}
