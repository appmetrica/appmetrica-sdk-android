package io.appmetrica.analytics.idsync.impl.precondition

import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PreconditionProviderTest : CommonTest() {

    private val serviceContext: ServiceContext = mock()
    private val preconditions: Preconditions = mock()

    @get:Rule
    val cellNetworkPreconditionVerifierRule = constructionRule<CellNetworkPreconditionVerifier>()

    @get:Rule
    val anyPreconditionVerifierRule = constructionRule<AnyPreconditionVerifier>()

    private val provider by setUp { PreconditionProvider(serviceContext) }

    @Test
    fun `getPrecondition for cell network`() {
        whenever(preconditions.networkType).doReturn(NetworkType.CELL)
        assertThat(provider.getPrecondition(preconditions))
            .isEqualTo(cellNetworkPreconditionVerifierRule.constructionMock.constructed().first())
        assertThat(cellNetworkPreconditionVerifierRule.constructionMock.constructed().size).isEqualTo(1)
        assertThat(cellNetworkPreconditionVerifierRule.argumentInterceptor.flatArguments())
            .containsExactly(serviceContext)
        assertThat(anyPreconditionVerifierRule.constructionMock.constructed().size).isEqualTo(0)
    }

    @Test
    fun `getPrecondition for any network`() {
        whenever(preconditions.networkType).doReturn(NetworkType.ANY)
        assertThat(provider.getPrecondition(preconditions))
            .isEqualTo(anyPreconditionVerifierRule.constructionMock.constructed().first())
        assertThat(cellNetworkPreconditionVerifierRule.constructionMock.constructed().size).isEqualTo(0)
        assertThat(anyPreconditionVerifierRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
