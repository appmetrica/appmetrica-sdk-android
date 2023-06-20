package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.location.toggles.ClientApiTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.VisibleAppStateOnlyTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.WakelocksToggle
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TogglesHolderTest : CommonTest() {

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

    private fun singleClientApiTrackingStatusToggle(): ClientApiTrackingStatusToggle {
        assertThat(clientApiTrackingStatusToggleMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(clientApiTrackingStatusToggleMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(GlobalServiceLocator.getInstance().servicePreferences)
        return clientApiTrackingStatusToggleMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun singleWakelockToggle(): WakelocksToggle {
        assertThat(wakelocksToggleMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(wakelocksToggleMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return wakelocksToggleMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun visibleAppStateTrackingStatusToggle(): VisibleAppStateOnlyTrackingStatusToggle {
        assertThat(visibleAppStateOnlyTrackingStatusToggleMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(visibleAppStateOnlyTrackingStatusToggleMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
        return visibleAppStateOnlyTrackingStatusToggleMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun conjunctionCompositeToggle(outerToggle: Toggle): ConjunctiveCompositeThreadSafeToggle {
        assertThat(conjuctionCompositeToggleMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(conjuctionCompositeToggleMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                listOf(
                    singleClientApiTrackingStatusToggle(),
                    singleWakelockToggle(),
                    outerToggle
                ),
                "loc-def"
            )
        return conjuctionCompositeToggleMockedConstructionRule.constructionMock.constructed().first()
    }
}
