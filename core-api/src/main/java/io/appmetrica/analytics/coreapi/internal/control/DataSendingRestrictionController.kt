package io.appmetrica.analytics.coreapi.internal.control

interface DataSendingRestrictionController {

    val isRestrictedForSdk: Boolean

    val isRestrictedForMainReporter: Boolean

    fun isRestrictedForReporter(apiKey: String): Boolean
}
