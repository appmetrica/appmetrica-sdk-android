package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.Intent
import io.appmetrica.analytics.internal.AppMetricaService
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class AppAppMetricaServiceWakeLockIntentProviderTest : CommonTest() {

    private val context = mock<Context>()
    private val action = "io.appmetrica.analytics.some.Action"

    @get:Rule
    val intentMockedRule = MockedConstructionRule(Intent::class.java)

    private val wakeLockIntentProvider = AppMetricaServiceWakeLockIntentProvider()

    @Test
    fun getWakeLockIntent() {
        assertThat(wakeLockIntentProvider.getWakeLockIntent(context, action))
            .isEqualTo(intentMockedRule.constructionMock.constructed()[0])
        assertThat(intentMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context, AppMetricaService::class.java)
        verify(intentMockedRule.constructionMock.constructed()[0]).action = action
    }
}
