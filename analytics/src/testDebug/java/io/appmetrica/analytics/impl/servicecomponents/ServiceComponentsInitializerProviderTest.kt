package io.appmetrica.analytics.impl.servicecomponents

import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ServiceComponentsInitializerProviderTest : CommonTest() {

    @get:Rule
    val sdkUtilsMockedRule = MockedStaticRule(SdkUtils::class.java)

    @get:Rule
    val defaultServiceComponentInitializerMockedRule =
        MockedConstructionRule(DefaultServiceComponentsInitializer::class.java)

    private lateinit var serviceComponentsInitializerProvider: ServiceComponentsInitializerProvider

    @Before
    fun setUp() {
        serviceComponentsInitializerProvider = ServiceComponentsInitializerProvider()
    }

    @Test
    fun getServiceComponentsInitializer() {
        assertThat(serviceComponentsInitializerProvider.getServiceComponentsInitializer())
            .isEqualTo(defaultServiceComponentInitializerMockedRule.constructionMock.constructed()[0])
        assertThat(defaultServiceComponentInitializerMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(defaultServiceComponentInitializerMockedRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
