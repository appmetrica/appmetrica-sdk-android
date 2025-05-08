package io.appmetrica.analytics.impl.clientcomponents

import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClientComponentsInitializerProviderTest : CommonTest() {

    @get:Rule
    val sdkUtilsMockedRule = MockedStaticRule(SdkUtils::class.java)

    @get:Rule
    val defaultClientComponentInitializerMockedRule =
        MockedConstructionRule(DefaultClientComponentsInitializer::class.java)

    private lateinit var provider: ClientComponentsInitializerProvider

    @Before
    fun setUp() {
        provider = ClientComponentsInitializerProvider()
    }

    @Test
    fun getServiceComponentsInitializer() {
        assertThat(provider.getClientComponentsInitializer())
            .isEqualTo(defaultClientComponentInitializerMockedRule.constructionMock.constructed()[0])
        assertThat(defaultClientComponentInitializerMockedRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(defaultClientComponentInitializerMockedRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
