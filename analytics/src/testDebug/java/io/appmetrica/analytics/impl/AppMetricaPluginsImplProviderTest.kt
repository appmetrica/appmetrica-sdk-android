package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.proxy.AppMetricaPluginsProxy
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

internal class AppMetricaPluginsImplProviderTest : CommonTest() {

    @get:Rule
    val pluginsProxyRule = constructionRule<AppMetricaPluginsProxy>()

    @Test
    fun getImpl() {
        val impl = AppMetricaPluginsImplProvider.impl
        assertThat(impl).isNotNull
        assertThat(AppMetricaPluginsImplProvider.impl).isSameAs(impl)
    }
}
