package io.appmetrica.analytics.coreutils.internal.services

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilityServiceProviderTest : CommonTest() {

    @get:Rule
    val firstExecutionConditionServiceImplMockedRule =
        MockedConstructionRule(FirstExecutionConditionServiceImpl::class.java)

    @get:Rule
    val activationBarrierMockedRule = MockedConstructionRule(WaitForActivationDelayBarrier::class.java)

    private val configuration = mock<UtilityServiceConfiguration>()

    @Test
    fun activationBarrier() {
        val utilityServiceProvider = UtilityServiceProvider()
        assertThat(activationBarrierMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(utilityServiceProvider.activationBarrier)
            .isEqualTo(activationBarrierMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun firstExecutionService() {
        val utilityServiceProvider = UtilityServiceProvider()
        assertThat(firstExecutionConditionServiceImplMockedRule.constructionMock.constructed()).isEmpty()
        assertThat(utilityServiceProvider.firstExecutionService)
            .isEqualTo(firstExecutionConditionServiceImplMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun activate() {
        val utilityServiceProvider = UtilityServiceProvider()
        utilityServiceProvider.initAsync()
        verify(activationBarrierMockedRule.constructionMock.constructed()[0]).activate()
    }

    @Test
    fun updateConfiguration() {
        val utilityServiceProvider = UtilityServiceProvider()
        utilityServiceProvider.updateConfiguration(configuration)
        verify(firstExecutionConditionServiceImplMockedRule.constructionMock.constructed()[0])
            .updateConfig(configuration)
    }
}
