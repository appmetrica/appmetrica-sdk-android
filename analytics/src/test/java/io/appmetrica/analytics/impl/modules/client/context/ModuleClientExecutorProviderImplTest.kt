package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

internal class ModuleClientExecutorProviderImplTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val moduleClientExecutorProvider by setUp {
        ModuleClientExecutorProviderImpl()
    }

    @Test
    fun defaultExecutor() {
        assertThat(moduleClientExecutorProvider.defaultExecutor)
            .isEqualTo(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor)
    }
}
