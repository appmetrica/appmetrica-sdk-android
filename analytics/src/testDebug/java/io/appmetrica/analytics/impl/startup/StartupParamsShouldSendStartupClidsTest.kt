package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.internal.IdentifiersResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsShouldSendStartupClidsTest(
    @Suppress("UNUSED_PARAMETER") clientClidsValue: IdentifiersResult?,
    clientIdentifiersHolder: ClientIdentifiersHolder,
    isOutdated: Boolean,
    clientClids: Map<String, String>?,
    shouldUpdateClids: Boolean,
    private val expected: ExpectedStartupResults
) : StartupParamsTestBase(
    clientIdentifiersHolder = clientIdentifiersHolder,
    isOutdated = isOutdated,
    requestedIdentifiers = listOf(Constants.StartupParamsCallbackKeys.CLIDS),
    clientClids = clientClids,
    shouldUpdateClids = shouldUpdateClids
) {

    companion object {
        @Parameterized.Parameters(name = "[{index}] clidsValue={0}, isOutdated={2}, shouldUpdateClids={4}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filledClientIdentifiersHolder with NULL_IDENTIFIER, not outdated
            arrayOf(
                NULL_IDENTIFIER,
                prepareFilledWithClids(NULL_IDENTIFIER),
                false,
                null,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with EMPTY_IDENTIFIER, not outdated
            arrayOf(
                EMPTY_IDENTIFIER,
                prepareFilledWithClids(EMPTY_IDENTIFIER),
                false,
                null,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder with RESPONSE_CLIDS, not outdated
            arrayOf(
                RESPONSE_CLIDS,
                prepareEmptyWithClids(RESPONSE_CLIDS),
                false,
                null,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder without changes
            arrayOf(
                null,
                prepareEmptyIdentifiersHolderMock(),
                false,
                null,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with RESPONSE_CLIDS, not outdated, no client clids
            arrayOf(
                RESPONSE_CLIDS,
                prepareFilledWithClids(RESPONSE_CLIDS),
                false,
                null,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with RESPONSE_CLIDS, not outdated, client clids, no update
            arrayOf(
                RESPONSE_CLIDS,
                prepareFilledWithClids(RESPONSE_CLIDS),
                false,
                StartupParamsTestUtils.CLIDS_MAP_1,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with RESPONSE_CLIDS, not outdated, client clids, should update
            arrayOf(
                RESPONSE_CLIDS,
                prepareFilledWithClids(RESPONSE_CLIDS),
                false,
                StartupParamsTestUtils.CLIDS_MAP_1,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder with RESPONSE_CLIDS, outdated, client clids, should update
            arrayOf(
                RESPONSE_CLIDS,
                prepareFilledWithClids(RESPONSE_CLIDS),
                true,
                StartupParamsTestUtils.CLIDS_MAP_1,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder with RESPONSE_CLIDS, not outdated, client clids, should update
            arrayOf(
                RESPONSE_CLIDS,
                prepareEmptyWithClids(RESPONSE_CLIDS),
                false,
                StartupParamsTestUtils.CLIDS_MAP_1,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // emptyClientIdentifiersHolder with RESPONSE_CLIDS, outdated, client clids, should update
            arrayOf(
                RESPONSE_CLIDS,
                prepareEmptyWithClids(RESPONSE_CLIDS),
                true,
                StartupParamsTestUtils.CLIDS_MAP_1,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            )
        )
    }

    @Test
    fun `shouldSendStartup for CLIDS`() {
        assertThat(startupParams.shouldSendStartup())
            .isEqualTo(expected.shouldSendStartupForAll)
    }
}
