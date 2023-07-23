package io.appmetrica.analytics.coreutils.internal.services

import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilityServiceLocatorTest {

    @get:Rule
    val firstExecutionConditionServiceMockedRule = MockedConstructionRule(FirstExecutionConditionService::class.java)

    @get:Rule
    val activationBarrierMockedRule = MockedConstructionRule(ActivationBarrier::class.java)

    val configuration = mock<UtilityServiceConfiguration>()

    @Test
    fun getInstance() {
        val firstValue = UtilityServiceLocator.instance
        val secondValue = UtilityServiceLocator.instance
        assertThat(firstValue).isSameAs(secondValue)
    }

    @Test
    fun activationBarrier() {
        val utilityServiceLocator = UtilityServiceLocator()
        assertThat(activationBarrierMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(utilityServiceLocator.activationBarrier)
            .isEqualTo(activationBarrierMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun firstExecutionService() {
        val utilityServiceLocator = UtilityServiceLocator()
        assertThat(firstExecutionConditionServiceMockedRule.constructionMock.constructed()).isEmpty()
        assertThat(utilityServiceLocator.firstExecutionService)
            .isEqualTo(firstExecutionConditionServiceMockedRule.constructionMock.constructed()[0])
    }

    @Test
    fun activate() {
        val utilityServiceLocator = UtilityServiceLocator()
        utilityServiceLocator.initAsync()
        verify(activationBarrierMockedRule.constructionMock.constructed()[0]).activate()
    }

    @Test
    fun updateConfiguration() {
        val utilityServiceLocator = UtilityServiceLocator()
        utilityServiceLocator.updateConfiguration(configuration)
        verify(firstExecutionConditionServiceMockedRule.constructionMock.constructed()[0])
            .updateConfig(configuration)
    }
}
