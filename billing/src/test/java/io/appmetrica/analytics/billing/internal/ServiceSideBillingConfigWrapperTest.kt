package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.billing.internal.ServiceSideBillingConfigWrapper.Companion.toWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class ServiceSideBillingConfigWrapperTest : CommonTest() {

    private val config: ServiceSideRemoteBillingConfig = mock()

    @Test
    fun toWrapper() {
        val wrapper = config.toWrapper()
        assertThat(wrapper.config).isSameAs(config)
    }
}
