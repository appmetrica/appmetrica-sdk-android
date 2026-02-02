package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsContainsIdentifiersAllTest(
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
    fun `containsIdentifiers for ALL_IDENTIFIERS`() {
        assertThat(startupParams.containsIdentifiers(requestedIdentifiers))
            .isEqualTo(expected.containsIdentifiers)
    }

    @Test
    fun `shouldSendStartup for ALL_IDENTIFIERS`() {
        assertThat(startupParams.shouldSendStartup(requestedIdentifiers))
            .isEqualTo(expected.shouldSendStartup)
    }

    @Test
    fun `shouldSendStartup without parameters`() {
        assertThat(startupParams.shouldSendStartup())
            .isEqualTo(expected.shouldSendStartupForAll)
    }
}
