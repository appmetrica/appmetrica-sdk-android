package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.location.toggles.ClientApiTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.VisibleAppStateOnlyTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.WakelocksToggle
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class TogglesHolderTest : CommonTest() {

    private val appStateToggle = mock<Toggle>()

    @get:Rule
    val clientApiTrackingStatusToggleMockedConstructionRule =
        MockedConstructionRule(ClientApiTrackingStatusToggle::class.java)

    @get:Rule
    val wakelocksToggleMockedConstructionRule = MockedConstructionRule(WakelocksToggle::class.java)

    @get:Rule
    val conjuctionCompositeToggleMockedConstructionRule =
        MockedConstructionRule(ConjunctiveCompositeThreadSafeToggle::class.java)

    @get:Rule
    val visibleAppStateOnlyTrackingStatusToggleMockedConstructionRule =
        MockedConstructionRule(VisibleAppStateOnlyTrackingStatusToggle::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private lateinit var togglesHolder: TogglesHolder

    @Test
    fun clientTrackingStatusController() {
        togglesHolder = TogglesHolder(appStateToggle)
        assertThat(togglesHolder.clientTrackingStatusController).isEqualTo(singleClientApiTrackingStatusToggle())
    }

    @Test
    fun wakelockToggle() {
        togglesHolder = TogglesHolder(appStateToggle)
        assertThat(togglesHolder.wakelocksToggle).isEqualTo(singleWakelockToggle())
    }

    @Test
    fun resultLocationControlToggle() {
        togglesHolder = TogglesHolder(appStateToggle)
        assertThat(togglesHolder.resultLocationControlToggle).isEqualTo(conjunctionCompositeToggle(appStateToggle))
    }

    @Test
    fun `resultLocationControlToggle without outer toggle`() {
        togglesHolder = TogglesHolder(null)
        assertThat(togglesHolder.resultLocationControlToggle)
            .isEqualTo(conjunctionCompositeToggle(visibleAppStateTrackingStatusToggle()))
    }

    private fun singleClientApiTrackingStatusToggle(): ClientApiTrackingStatusToggle =
        clientApiTrackingStatusToggleMockedConstructionRule
            .singleWithArgs(GlobalServiceLocator.getInstance().servicePreferences)

    private fun singleWakelockToggle(): WakelocksToggle = wakelocksToggleMockedConstructionRule.singleWithArgs()

    private fun visibleAppStateTrackingStatusToggle(): VisibleAppStateOnlyTrackingStatusToggle =
        visibleAppStateOnlyTrackingStatusToggleMockedConstructionRule.singleWithArgs()

    private fun conjunctionCompositeToggle(outerToggle: Toggle): ConjunctiveCompositeThreadSafeToggle =
        conjuctionCompositeToggleMockedConstructionRule.singleWithArgs(
            listOf(
                singleClientApiTrackingStatusToggle(),
                singleWakelockToggle(),
                outerToggle
            ),
            "loc-def"
        )
}
