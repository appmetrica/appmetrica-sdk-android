package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsShouldSendStartupForIdentifiersAllTest(
    clientIdentifiersHolder: ClientIdentifiersHolder,
    isOutdated: Boolean,
    private val expected: ExpectedStartupResults
) : StartupParamsTestBase(
    clientIdentifiersHolder = clientIdentifiersHolder,
    isOutdated = isOutdated,
    requestedIdentifiers = StartupParamsTestUtils.ALL_IDENTIFIERS,
    clientClids = null,
    shouldUpdateClids = true
) {

    companion object {
        @Parameterized.Parameters(name = "[{index}] holder={0}, isOutdated={1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filledClientIdentifiersHolder, isOutdated=false
            arrayOf(
                prepareFilledIdentifiersHolderMock(),
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, isOutdated=true
            arrayOf(
                prepareFilledIdentifiersHolderMock(),
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder, isOutdated=false
            arrayOf(
                prepareEmptyIdentifiersHolderMock(),
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
    fun `shouldSendStartup for identifiers ALL_IDENTIFIERS`() {
        assertThat(startupParams.shouldSendStartup(requestedIdentifiers))
            .isEqualTo(expected.shouldSendStartup)
    }
}
