package io.appmetrica.analytics.impl

internal interface ShouldDisconnectFromServiceChecker {

    fun shouldDisconnect(): Boolean
}
