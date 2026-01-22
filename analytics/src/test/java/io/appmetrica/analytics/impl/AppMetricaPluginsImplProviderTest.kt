package io.appmetrica.analytics.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class AppMetricaPluginsImplProviderTest : CommonTest() {

    @Test
    fun getImpl() {
        val impl = AppMetricaPluginsImplProvider.impl
        assertThat(impl).isNotNull
        assertThat(AppMetricaPluginsImplProvider.impl).isSameAs(impl)
    }
}
