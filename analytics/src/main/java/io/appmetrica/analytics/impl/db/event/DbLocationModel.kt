package io.appmetrica.analytics.impl.db.event

class DbLocationModel(
    val enabled: Boolean?,
    val longitude: Double?,
    val latitude: Double?,
    val altitude: Int?,
    val direction: Int?,
    val precision: Int?,
    val speed: Int?,
    val timestamp: Long?,
    val provider: String?,
    val originalProvider: String?,
)
