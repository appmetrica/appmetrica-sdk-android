package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ComponentEventConditionsProviderTest : CommonTest() {

    private val databaseHelper: DatabaseHelper = mock()

    private val maxReportsCount = 40
    private val reportRequestConfig: ReportRequestConfig = mock {
        on { maxReportsCount } doReturn maxReportsCount
    }
    private val configurationHolder: ReportComponentConfigurationHolder = mock {
        on { get() } doReturn reportRequestConfig
    }

    @get:Rule
    val pendingReportsCountHolderRule = constructionRule<PendingReportsCountHolder>()
    private val pendingReportsCountHolder by pendingReportsCountHolderRule

    @get:Rule
    val containsUrgentEventsConditionRule = constructionRule<ContainsUrgentEventsCondition>()
    private val containsUrgentEventsCondition by containsUrgentEventsConditionRule

    @get:Rule
    val maxReportsCountReachedConditionRule = constructionRule<MaxReportsCountReachedCondition>()

    private val componentEventConditionsProvider by setUp {
        ComponentEventConditionsProvider(databaseHelper, configurationHolder)
    }

    @Test
    fun getCommonEventConditions() {
        assertThat(componentEventConditionsProvider.getCommonEventConditions()).containsExactly(
            containsUrgentEventsCondition, maxReportsCountReachedConditionRule.constructionMock.constructed().first()
        )

        assertThat(containsUrgentEventsConditionRule.constructionMock.constructed()).hasSize(1)
        assertThat(containsUrgentEventsConditionRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseHelper)

        assertThat(maxReportsCountReachedConditionRule.constructionMock.constructed()).hasSize(2)
        assertThat(maxReportsCountReachedConditionRule.argumentInterceptor.arguments).hasSize(2)

        val maxReportsConditionArguments = maxReportsCountReachedConditionRule.argumentInterceptor.arguments.first()

        assertThat(maxReportsConditionArguments.first()).isEqualTo(pendingReportsCountHolder)
        @Suppress("UNCHECKED_CAST")
        assertThat((maxReportsConditionArguments[1] as () -> Int).invoke()).isEqualTo(maxReportsCount)
    }

    @Test
    fun getForceSendEventConditions() {
        assertThat(componentEventConditionsProvider.getForceSendEventConditions()).containsExactly(
            maxReportsCountReachedConditionRule.constructionMock.constructed()[1]
        )

        assertThat(maxReportsCountReachedConditionRule.constructionMock.constructed()).hasSize(2)
        assertThat(maxReportsCountReachedConditionRule.argumentInterceptor.arguments).hasSize(2)

        val hasPendingEventsArguments = maxReportsCountReachedConditionRule.argumentInterceptor.arguments[1]

        assertThat(hasPendingEventsArguments.first()).isEqualTo(pendingReportsCountHolder)
        @Suppress("UNCHECKED_CAST")
        assertThat((hasPendingEventsArguments[1] as () -> Int).invoke()).isEqualTo(1)
    }
}
