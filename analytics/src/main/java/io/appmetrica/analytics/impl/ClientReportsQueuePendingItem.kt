package io.appmetrica.analytics.impl

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

internal class ClientReportsQueuePendingItem(
    val category: ClientReportsQueueCategory,
    val weight: Long,
    var callable: Callable<*>?,
    var reservation: ClientReportsQueueReservation? = null,
    var started: Boolean = false,
    val completionLatch: CountDownLatch? = null,
) {
    fun clearRefs() {
        callable = null
        reservation = null
    }
}
