package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.internal.IdentifiersResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsShouldSendStartupForIdentifiersHoaidTest(
    identifierValue: IdentifiersResult?,
    isOutdated: Boolean,
    private val expected: ExpectedStartupResults
) : StartupParamsTestBase(
    clientIdentifiersHolder = prepareClientIdentifiersHolder(identifierValue),
    isOutdated = isOutdated,
    requestedIdentifiers = listOf(Constants.StartupParamsCallbackKeys.HUAWEI_ADV_ID),
    clientClids = null,
    shouldUpdateClids = shouldUpdateClids(identifierValue)
) {

    companion object {

        private fun prepareClientIdentifiersHolder(identifierValue: IdentifiersResult?) = when (identifierValue) {
            null -> prepareEmptyIdentifiersHolderMock()
            HOAID -> prepareEmptyWithHoaid(identifierValue)
            else -> prepareFilledWithHoaid(identifierValue)
        }

        private fun shouldUpdateClids(identifierValue: IdentifiersResult?) =
            identifierValue != null && identifierValue != HOAID

        @Parameterized.Parameters(name = "[{index}] hoaid={0}, isOutdated={1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filled with NULL_IDENTIFIER, not outdated
            arrayOf(
                NULL_IDENTIFIER,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = true
                )
            ),
            // filled with NULL_IDENTIFIER, outdated
            arrayOf(
                NULL_IDENTIFIER,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filled with EMPTY_IDENTIFIER, not outdated
            arrayOf(
                EMPTY_IDENTIFIER,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = true
                )
            ),
            // filled with EMPTY_IDENTIFIER, outdated
            arrayOf(
                EMPTY_IDENTIFIER,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // empty with HOAID, not outdated
            arrayOf(
                HOAID,
                false,
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
    }

    @Test
    fun `shouldSendStartup for identifiers HOAID`() {
        assertThat(startupParams.shouldSendStartup(requestedIdentifiers))
            .isEqualTo(expected.shouldSendStartup)
    }
}
