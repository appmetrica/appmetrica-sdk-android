package io.appmetrica.analytics.impl.db

internal interface VitalDataSource {

    fun getVitalData(): String?

    fun putVitalData(data: String)

    fun flush()

    fun flushAsync()
}
