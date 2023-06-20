package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceConfiguration
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.rules.coreutils.UtilityServiceLocatorRule
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

    @get:Rule
    val updateConfigurationMockedRule = MockedConstructionRule(UtilityServiceConfiguration::class.java)
    @get:Rule
    val utilityServiceLocatorRule = UtilityServiceLocatorRule()

    private lateinit var utilityServiceStartupStateObserver: UtilityServiceStartupStateObserver

    @Before
    fun setUp() {
        utilityServiceStartupStateObserver = UtilityServiceStartupStateObserver()
    }

    @Test
    fun onStartupChanged() {
        utilityServiceStartupStateObserver.onStartupStateChanged(startupState)

        assertThat(updateConfigurationMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(updateConfigurationMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(firstStartupServerTime, obtainServerTime)
        verify(UtilityServiceLocator.instance)
            .updateConfiguration(updateConfigurationMockedRule.constructionMock.constructed()[0])
    }
}
