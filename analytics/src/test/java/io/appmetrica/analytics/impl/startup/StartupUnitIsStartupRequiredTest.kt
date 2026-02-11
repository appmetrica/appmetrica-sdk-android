package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class StartupUnitIsStartupRequiredTest : StartupUnitBaseTest() {

    @get:Rule
    val startupRequiredUtilsMockedStaticRule = staticRule<StartupRequiredUtils>()

    private val clientClids: Map<String, String> = HashMap()

    @Before
    fun setUp() {
        super.setup()
        whenever(startupRequestConfig.clidsFromClient).thenReturn(clientClids)
    }

    @Test
    fun startupIsNotRequired() {
        val startupState = mock<StartupState>()
        whenever(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false)
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true)
        whenever(
            clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clientClids,
                startupState,
                clidsStorage
            )
        ).thenReturn(true)
        assertThat(startupUnit.isStartupRequired()).isFalse()
    }
    @Test
    fun startupIsRequiredBecauseClidsDoNotMatch() {
        val startupState = mock<StartupState>()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false)
        whenever(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true)
        whenever(
            clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clientClids,
                startupState,
                clidsStorage
            )
        ).thenReturn(false)
        assertThat(startupUnit.isStartupRequired()).isTrue()
    }

    @Test
    fun startupIsRequiredBecauseMainIdentifiersArNotValid() {
        val startupState = mock<StartupState>()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false)
        whenever(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(false)
        whenever(
            clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clientClids,
                startupState,
                clidsStorage
            )
        ).thenReturn(true)
        assertThat(startupUnit.isStartupRequired()).isTrue()
    }

    @Test
    fun startupIsRequiredBecauseItIsOutdated() {
        val startupState = mock<StartupState>()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(StartupRequiredUtils.isOutdated(startupState)).thenReturn(true)
        whenever(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true)
        whenever(
            clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clientClids,
                startupState,
                clidsStorage
            )
        ).thenReturn(true)
        assertThat(startupUnit.isStartupRequired()).isTrue()
    }

    @Test
    fun startupIsRequiredIfIsOutdatedDataIsRestricted() {
        val startupState = mock<StartupState>()
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(StartupRequiredUtils.isOutdated(startupState)).thenReturn(true)
        whenever(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true)
        whenever(
            clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(
                clientClids,
                startupState,
                clidsStorage
            )
        ).thenReturn(true)
        whenever(GlobalServiceLocator.getInstance().dataSendingRestrictionController.isRestrictedForSdk)
            .thenReturn(true)
        assertThat(startupUnit.isStartupRequired()).isFalse()
    }

    @Test
    fun isStartupRequiredForIdentifiersRequired() {
        val startupState = mock<StartupState>()
        val identifiers: List<String> = mutableListOf("uuid", "device_id")
        val clids: MutableMap<String, String> = HashMap()
        clids["clid0"] = "0"
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(
            StartupRequiredUtils.containsIdentifiers(
                eq(startupState),
                eq(identifiers),
                eq<Map<String, String>>(clids),
                any()
            )
        ).thenReturn(false)
        assertThat(startupUnit.isStartupRequired(identifiers, clids)).isTrue()
    }

    @Test
    fun isStartupRequiredForIdentifiersNotRequired() {
        val startupState = mock<StartupState>()
        val identifiers: List<String> = mutableListOf("uuid", "device_id")
        val clids: MutableMap<String, String> = HashMap()
        clids["clid0"] = "0"
        whenever(startupConfigurationHolder.startupState).thenReturn(startupState)
        whenever(
            StartupRequiredUtils.containsIdentifiers(
                eq(startupState),
                eq(identifiers),
                eq<Map<String, String>>(clids),
                any()
            )
        ).thenReturn(true)
        assertThat(startupUnit.isStartupRequired(identifiers, clids)).isFalse()
    }
}
