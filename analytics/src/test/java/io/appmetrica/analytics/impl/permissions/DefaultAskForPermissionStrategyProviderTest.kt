package io.appmetrica.analytics.impl.permissions

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class DefaultAskForPermissionStrategyProviderTest : CommonTest() {

    @get:Rule
    val neverForbidPermissionStrategyMockedConstructionRule =
        MockedConstructionRule(NeverForbidPermissionStrategy::class.java)

    private lateinit var defaultAskForPermissionStrategyProvider: DefaultAskForPermissionStrategyProvider

    @Before
    fun setUp() {
        defaultAskForPermissionStrategyProvider = DefaultAskForPermissionStrategyProvider()
    }

    @Test
    fun askForPermissionStrategy() {
        assertThat(defaultAskForPermissionStrategyProvider.askForPermissionStrategy)
            .isEqualTo(neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed().first())
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(neverForbidPermissionStrategyMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
