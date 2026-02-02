package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.internal.IdentifiersResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsContainsIdentifiersCustomHostsTest(
    @Suppress("UNUSED_PARAMETER") customHostsValue: IdentifiersResult?,
    clientIdentifiersHolder: ClientIdentifiersHolder,
    requestedIdentifiers: List<String>,
    isOutdated: Boolean,
    private val expected: ExpectedStartupResults
) : StartupParamsTestBase(
    clientIdentifiersHolder = clientIdentifiersHolder,
    isOutdated = isOutdated,
    requestedIdentifiers = requestedIdentifiers,
    clientClids = StartupParamsTestUtils.CLIDS_MAP_1,
    shouldUpdateClids = true
) {

    companion object {
        private val EMPTY_CUSTOM_SDK_HOSTS = IdentifiersResult(null, IdentifierStatus.UNKNOWN, null)

        @Parameterized.Parameters(name = "[{index}] customHostsValue={0}, requestedIdentifiers={2}, isOutdated={3}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filledClientIdentifiersHolder, CUSTOM_IDENTIFIERS, not outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder, CUSTOM_IDENTIFIERS, outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder, CUSTOM_IDENTIFIERS, not outdated
            arrayOf(
                null,
                prepareEmptyIdentifiersHolderMock(),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // emptyClientIdentifiersHolder, CUSTOM_IDENTIFIERS, outdated
            arrayOf(
                null,
                prepareEmptyIdentifiersHolderMock(),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with empty custom SDK hosts, CUSTOM_IDENTIFIERS, not outdated
            arrayOf(
                EMPTY_CUSTOM_SDK_HOSTS,
                prepareFilledWithCustomSdkHosts(EMPTY_CUSTOM_SDK_HOSTS),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder with empty custom SDK hosts, CUSTOM_IDENTIFIERS, outdated
            arrayOf(
                EMPTY_CUSTOM_SDK_HOSTS,
                prepareFilledWithCustomSdkHosts(EMPTY_CUSTOM_SDK_HOSTS),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder with CUSTOM_SDK_HOSTS, CUSTOM_IDENTIFIERS, not outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareEmptyWithCustomSdkHosts(CUSTOM_SDK_HOSTS),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // emptyClientIdentifiersHolder with CUSTOM_SDK_HOSTS, CUSTOM_IDENTIFIERS, outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareEmptyWithCustomSdkHosts(CUSTOM_SDK_HOSTS),
                StartupParamsTestUtils.CUSTOM_IDENTIFIERS,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM, not outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM, outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS, not outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS, outdated
            arrayOf(
                CUSTOM_SDK_HOSTS,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_EXCEPT_ADS,
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
    fun `containsIdentifiers for custom SDK hosts`() {
        assertThat(startupParams.containsIdentifiers(requestedIdentifiers))
            .isEqualTo(expected.containsIdentifiers)
    }

    @Test
    fun `shouldSendStartup for custom SDK hosts`() {
        assertThat(startupParams.shouldSendStartup(requestedIdentifiers))
            .isEqualTo(expected.shouldSendStartup)
    }

    @Test
    fun `shouldSendStartup without parameters`() {
        assertThat(startupParams.shouldSendStartup())
            .isEqualTo(expected.shouldSendStartupForAll)
    }
}
