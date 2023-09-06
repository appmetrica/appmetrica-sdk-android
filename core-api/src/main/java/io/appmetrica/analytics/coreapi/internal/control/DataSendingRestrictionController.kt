package io.appmetrica.analytics.coreapi.internal.control

interface DataSendingRestrictionController {

    val isRestrictedForReporter: Boolean

    val isRestrictedForSdk: Boolean

    val isRestrictedForBackgroundDataCollection: Boolean
}
