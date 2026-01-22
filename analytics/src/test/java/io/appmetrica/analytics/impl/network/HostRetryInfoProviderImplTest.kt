package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

internal class HostRetryInfoProviderImplTest : CommonTest() {

    private val nextSendAttemptNumber = 124
    private val lastAttemptTimeSeconds = 150L

    private val host = mock<NetworkHost>()

    private val servicePreferences = mock<PreferencesServiceDbStorage> {
        on { getNextSendAttemptNumber(host, 1) } doReturn nextSendAttemptNumber
        on { getLastSendAttemptTimeSeconds(host, 0) } doReturn lastAttemptTimeSeconds
        on { putNextSendAttemptNumber(any(), any()) } doReturn mock
        on { putLastSendAttemptTimeSeconds(any(), any()) } doReturn mock
    }

    private val retryInfoProvider = HostRetryInfoProviderImpl(servicePreferences, host)

    @Test
    fun getNextSendAttemptNumber() {
        assertThat(retryInfoProvider.nextSendAttemptNumber).isEqualTo(nextSendAttemptNumber)
    }

    @Test
    fun getLastAttemptTimeSeconds() {
        assertThat(retryInfoProvider.lastAttemptTimeSeconds).isEqualTo(lastAttemptTimeSeconds)
    }

    @Test
    fun saveNextSendAttemptNumber() {
        retryInfoProvider.saveNextSendAttemptNumber(nextSendAttemptNumber)
        inOrder(servicePreferences) {
            verify(servicePreferences).putNextSendAttemptNumber(host, nextSendAttemptNumber)
            verify(servicePreferences).commit()
        }
    }

    @Test
    fun savaLastAttemptTimeSeconds() {
        retryInfoProvider.saveLastAttemptTimeSeconds(lastAttemptTimeSeconds)
        inOrder(servicePreferences) {
            verify(servicePreferences).putLastSendAttemptTimeSeconds(host, lastAttemptTimeSeconds)
            verify(servicePreferences).commit()
        }
    }
}
