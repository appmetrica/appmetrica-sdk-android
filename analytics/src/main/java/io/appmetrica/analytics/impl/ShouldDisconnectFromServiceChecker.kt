package io.appmetrica.analytics.impl

interface ShouldDisconnectFromServiceChecker {

    fun shouldDisconnect(): Boolean
}
