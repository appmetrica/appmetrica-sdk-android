package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class StartupParamsShouldSendStartupFeaturesTest(
    @Suppress("UNUSED_PARAMETER") featuresValue: FeaturesInternal?,
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
        private val EMPTY_FEATURES = FeaturesInternal(null, IdentifierStatus.UNKNOWN, null)

        @Parameterized.Parameters(name = "[{index}] featuresValue={0}, requestedIdentifiers={2}, isOutdated={3}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            // filledClientIdentifiersHolder, IDENTIFIERS_WITH_SSL_FEATURE, not outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder, IDENTIFIERS_WITH_SSL_FEATURE, outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder, IDENTIFIERS_WITH_SSL_FEATURE, not outdated
            arrayOf(
                null,
                prepareEmptyIdentifiersHolderMock(),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // emptyClientIdentifiersHolder, IDENTIFIERS_WITH_SSL_FEATURE, outdated
            arrayOf(
                null,
                prepareEmptyIdentifiersHolderMock(),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder with empty features, IDENTIFIERS_WITH_SSL_FEATURE, not outdated
            arrayOf(
                EMPTY_FEATURES,
                prepareFilledWithFeatures(EMPTY_FEATURES),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder with empty features, IDENTIFIERS_WITH_SSL_FEATURE, outdated
            arrayOf(
                EMPTY_FEATURES,
                prepareFilledWithFeatures(EMPTY_FEATURES),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = false,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // emptyClientIdentifiersHolder with FEATURES, IDENTIFIERS_WITH_SSL_FEATURE, not outdated
            arrayOf(
                FEATURES,
                prepareEmptyWithFeatures(FEATURES),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = false
                )
            ),
            // emptyClientIdentifiersHolder with FEATURES, IDENTIFIERS_WITH_SSL_FEATURE, outdated
            arrayOf(
                FEATURES,
                prepareEmptyWithFeatures(FEATURES),
                StartupParamsTestUtils.IDENTIFIERS_WITH_SSL_FEATURE,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, not outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE, outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE,
                true,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = true,
                    shouldSendStartup = true
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, not outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV,
                false,
                ExpectedStartupResults(
                    containsIdentifiers = true,
                    shouldSendStartupForAll = false,
                    shouldSendStartup = false
                )
            ),
            // filledClientIdentifiersHolder, ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV, outdated
            arrayOf(
                FEATURES,
                prepareFilledIdentifiersHolderMock(),
                StartupParamsTestUtils.ALL_IDENTIFIERS_WITH_CUSTOM_AND_FEATURE_EXCEPT_ADV,
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
    fun `shouldSendStartup for features`() {
        assertThat(startupParams.shouldSendStartup())
            .isEqualTo(expected.shouldSendStartupForAll)
    }
}
