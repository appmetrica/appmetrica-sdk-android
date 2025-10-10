package io.appmetrica.analytics.idsync.impl.precondition

internal interface PreconditionVerifier {

    fun matchPrecondition(): Boolean
}
