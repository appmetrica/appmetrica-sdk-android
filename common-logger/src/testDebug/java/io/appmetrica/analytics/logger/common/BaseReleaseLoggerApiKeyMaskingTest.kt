package io.appmetrica.analytics.logger.common

import io.appmetrica.analytics.logger.common.impl.LogMessageConstructor
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

internal class BaseReleaseLoggerApiKeyMaskingTest : CommonTest() {

    @get:Rule
    val logMessageConstructorRule = constructionRule<LogMessageConstructor>()

    @Test
    fun usesApiKeyMaskingLogMessageConstructor() {
        object : BaseReleaseLogger("AppMetrica", "[prefix]") {}

        assertThat(logMessageConstructorRule.argumentInterceptor.flatArguments())
            .containsExactly(true)
    }
}
