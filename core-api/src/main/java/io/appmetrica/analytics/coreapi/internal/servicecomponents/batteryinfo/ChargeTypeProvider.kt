package io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo

interface ChargeTypeProvider {

    val chargeType: ChargeType

    fun registerChargeTypeListener(listener: ChargeTypeChangeListener)

    val batteryLevel: Int?
}
