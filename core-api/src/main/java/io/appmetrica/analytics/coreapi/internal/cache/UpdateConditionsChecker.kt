package io.appmetrica.analytics.coreapi.internal.cache

interface UpdateConditionsChecker {

    fun shouldUpdate(): Boolean
}
