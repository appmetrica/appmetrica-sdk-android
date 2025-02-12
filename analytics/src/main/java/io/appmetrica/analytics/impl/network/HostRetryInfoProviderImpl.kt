package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.networktasks.internal.HostRetryInfoProvider

internal class HostRetryInfoProviderImpl(
    private val servicePreferences: PreferencesServiceDbStorage,
    val host: NetworkHost
) : HostRetryInfoProvider {

    override fun getNextSendAttemptNumber(): Int {
        return servicePreferences.getNextSendAttemptNumber(host, 1)
    }

    override fun getLastAttemptTimeSeconds(): Long {
        return servicePreferences.getLastSendAttemptTimeSeconds(host, 0)
    }

    override fun saveNextSendAttemptNumber(nextSendAttemptNumber: Int) {
        servicePreferences.putNextSendAttemptNumber(host, nextSendAttemptNumber).commit()
    }

    override fun saveLastAttemptTimeSeconds(lastAttemptTimeSeconds: Long) {
        servicePreferences.putLastSendAttemptTimeSeconds(host, lastAttemptTimeSeconds).commit()
    }
}
