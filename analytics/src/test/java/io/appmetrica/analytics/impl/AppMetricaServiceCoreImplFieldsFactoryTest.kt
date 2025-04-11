package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.impl.component.clients.ClientRepository
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class AppMetricaServiceCoreImplFieldsFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val clientRepository: ClientRepository = mock()

    @get:Rule
    val reportConsumerMockedConstructionRule = constructionRule<ReportConsumer>()

    private val factory: AppMetricaServiceCoreImplFieldsFactory by setUp { AppMetricaServiceCoreImplFieldsFactory() }

    @Test
    fun createReportConsumer() {
        assertThat(factory.createReportConsumer(context, clientRepository))
            .isEqualTo(reportConsumerMockedConstructionRule.constructionMock.constructed().first())

        assertThat(reportConsumerMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reportConsumerMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, clientRepository)
    }

}
