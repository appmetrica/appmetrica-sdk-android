package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceConfiguration
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceProvider
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class UtilityServiceStartupStateObserverTest {

    private val firstStartupServerTime = 123L
    private val obtainServerTime = 14534221L
    private val startupState = mock<StartupState> {
        on { firstStartupServerTime } doReturn firstStartupServerTime
        on { obtainServerTime } doReturn obtainServerTime
    }

    private val utilitiesServiceProvider: UtilityServiceProvider = mock()

    @get:Rule
    val updateConfigurationMockedRule = MockedConstructionRule(UtilityServiceConfiguration::class.java)

    private lateinit var utilityServiceStartupStateObserver: UtilityServiceStartupStateObserver

    @Before
    fun setUp() {
        utilityServiceStartupStateObserver = UtilityServiceStartupStateObserver(utilitiesServiceProvider)
    }

    @Test
    fun onStartupChanged() {
        utilityServiceStartupStateObserver.onStartupStateChanged(startupState)

        assertThat(updateConfigurationMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(updateConfigurationMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(firstStartupServerTime, obtainServerTime)
        verify(utilitiesServiceProvider)
            .updateConfiguration(updateConfigurationMockedRule.constructionMock.constructed()[0])
    }
}
