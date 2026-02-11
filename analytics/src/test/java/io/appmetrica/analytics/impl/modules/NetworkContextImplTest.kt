package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.network.CompositeExecutionPolicy
import io.appmetrica.analytics.impl.network.ConnectionBasedExecutionPolicy
import io.appmetrica.analytics.impl.network.ReporterRestrictionBasedPolicy
import io.appmetrica.analytics.impl.network.UserAgentProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class NetworkContextImplTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val connectionBasePolicyRule = constructionRule<ConnectionBasedExecutionPolicy>()

    @get:Rule
    val reporterRestrictionBasedPolicyRule = constructionRule<ReporterRestrictionBasedPolicy>()

    @get:Rule
    val compositeExecutionPolicyRule = constructionRule<CompositeExecutionPolicy>()

    @get:Rule
    val networkApiMockedConstructionRule = constructionRule<SimpleNetworkApiImpl>()

    @get:Rule
    val userAgentProviderRule = constructionRule<UserAgentProvider>()

    private val context = mock<Context>()

    private val networkContextImpl by setUp { NetworkContextImpl(context) }

    @Test
    fun sslSocketFactoryProvider() {
        assertThat(networkContextImpl.sslSocketFactoryProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().sslSocketFactoryProvider)
    }

    @Test
    fun executionPolicy() {
        assertThat(networkContextImpl.executionPolicy)
            .isEqualTo(compositeExecutionPolicyRule.constructionMock.constructed()[0])
        assertThat(compositeExecutionPolicyRule.constructionMock.constructed()).hasSize(1)
        assertThat(compositeExecutionPolicyRule.argumentInterceptor.flatArguments()).containsExactly(
            arrayOf(
                connectionBasePolicyRule.constructionMock.constructed().first(),
                reporterRestrictionBasedPolicyRule.constructionMock.constructed().first()
            )
        )

        assertThat(connectionBasePolicyRule.constructionMock.constructed()).hasSize(1)
        assertThat(connectionBasePolicyRule.argumentInterceptor.flatArguments())
            .containsExactly(context)

        assertThat(reporterRestrictionBasedPolicyRule.constructionMock.constructed()).hasSize(1)
        assertThat(reporterRestrictionBasedPolicyRule.argumentInterceptor.flatArguments())
            .containsExactly(GlobalServiceLocator.getInstance().dataSendingRestrictionController)
    }

    @Test
    fun networkApi() {
        assertThat(networkContextImpl.networkApi)
            .isSameAs(networkApiMockedConstructionRule.constructionMock.constructed().first())
        assertThat(networkApiMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(networkApiMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
    }
}
