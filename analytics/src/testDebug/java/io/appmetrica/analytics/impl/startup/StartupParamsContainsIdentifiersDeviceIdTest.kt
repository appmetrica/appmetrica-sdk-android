package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.internal.IdentifiersResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsContainsIdentifiersDeviceIdTest(
    deviceIdValue: IdentifiersResult?,
    isOutdated: Boolean,
    private val expected: ExpectedStartupResults
) : StartupParamsTestBase(
    clientIdentifiersHolder = prepareClientIdentifiersHolder(deviceIdValue),
    isOutdated = isOutdated,
    requestedIdentifiers = listOf(Constants.StartupParamsCallbackKeys.DEVICE_ID),
    clientClids = null,
    shouldUpdateClids = shouldUpdateClids(deviceIdValue)
) {

    companion object {

        @Parameterized.Parameters(name = "[{index}] deviceId={0}, isOutdated={1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filled with NULL_IDENTIFIER
            arrayOf(
                NULL_IDENTIFIER,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filled with EMPTY_IDENTIFIER
            arrayOf(
                EMPTY_IDENTIFIER,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // empty with DEVICE_ID, not outdated
            arrayOf(
                DEVICE_ID,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // empty with DEVICE_ID, outdated
            arrayOf(
                DEVICE_ID,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // empty without changes
            arrayOf(
                null,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            )
        )

        private fun prepareClientIdentifiersHolder(deviceIdValue: IdentifiersResult?) = when (deviceIdValue) {
            null -> prepareEmptyIdentifiersHolderMock()
            DEVICE_ID -> prepareEmptyWithDeviceId(deviceIdValue)
            else -> prepareFilledWithDeviceId(deviceIdValue)
        }

        private fun shouldUpdateClids(deviceIdValue: IdentifiersResult?) =
            deviceIdValue != null && deviceIdValue != DEVICE_ID
    }

    @Test
    fun `containsIdentifiers for Device ID`() {
        assertThat(startupParams.containsIdentifiers(requestedIdentifiers))
            .isEqualTo(expected.containsIdentifiers)
    }
}
