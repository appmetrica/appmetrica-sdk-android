package io.appmetrica.analytics.impl.referrer.service

import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo

// Modifier 'internal' is not applicable inside 'interface'.
/* ktlint-disable appmetrica-rules:internal-modifier-in-impl-package */
internal sealed interface ReferrerResult {
    val referrerInfo: ReferrerInfo?

    class Success(override val referrerInfo: ReferrerInfo) : ReferrerResult {
        override fun toString(): String {
            return "Success(" +
                "referrerInfo=$referrerInfo" +
                ")"
        }
    }

    class Failure(val message: String, val throwable: Throwable? = null) : ReferrerResult {
        override val referrerInfo: ReferrerInfo? = null
        override fun toString(): String {
            return "Failure(" +
                "message='$message', " +
                "throwable=$throwable" +
                ")"
        }
    }
}
/* ktlint-enable appmetrica-rules:internal-modifier-in-impl-package */
