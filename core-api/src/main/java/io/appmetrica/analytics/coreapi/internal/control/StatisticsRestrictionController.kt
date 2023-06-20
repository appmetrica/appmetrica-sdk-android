package io.appmetrica.analytics.coreapi.internal.control

interface StatisticsRestrictionController {

    val isRestrictedForReporter: Boolean

    val isRestrictedForSdk: Boolean

    val isRestrictedForBackgroundDataCollection: Boolean
}
