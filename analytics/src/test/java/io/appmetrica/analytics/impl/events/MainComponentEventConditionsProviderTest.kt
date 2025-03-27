package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MainComponentEventConditionsProviderTest : CommonTest() {
    private val databaseHelper: DatabaseHelper = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val eventTriggerProvider: EventTriggerProvider = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    private val firstCommonCondition: EventCondition = mock()
    private val secondCommonCondition: EventCondition = mock()
    private val commonConditions = listOf(firstCommonCondition, secondCommonCondition)

    private val forceSendCommonCondition: EventCondition = mock()
    private val forceSendCommonConditions = listOf(forceSendCommonCondition)

    @get:Rule
    val componentEventConditionsProviderRule = constructionRule<ComponentEventConditionsProvider> {
        on { getCommonEventConditions() } doReturn commonConditions
        on { getForceSendEventConditions() } doReturn forceSendCommonConditions
    }

    private val mainReporterPolicyCondition: EventCondition = mock()
    private val policy: MainReporterEventSendingPolicy = mock {
        on { condition } doReturn mainReporterPolicyCondition
    }

    @get:Rule
    val mainReporterEventSendingPolicyProviderRule = constructionRule<MainReporterEventSendingPolicyProvider> {
        on { getPolicy(eventTriggerProvider, configurationHolder, initialConfig, preferences) } doReturn policy
    }

    private val mainComponentEventConditionProvider: MainComponentEventConditionsProvider by setUp {
        MainComponentEventConditionsProvider(
            databaseHelper,
            configurationHolder,
            eventTriggerProvider,
            initialConfig,
            preferences
        )
    }

    @Test
    fun componentEventConditionProvider() {
        assertThat(componentEventConditionsProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentEventConditionsProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseHelper, configurationHolder)
    }

    @Test
    fun mainReporterEventSendingPolicyProvider() {
        assertThat(mainReporterEventSendingPolicyProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainReporterEventSendingPolicyProviderRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun getCommonEventConditions() {
        assertThat(mainComponentEventConditionProvider.getCommonEventConditions())
            .containsExactly(firstCommonCondition, secondCommonCondition)
    }

    @Test
    fun getForceSendEventConditions() {
        assertThat(mainComponentEventConditionProvider.getForceSendEventConditions())
            .containsExactly(forceSendCommonCondition, mainReporterPolicyCondition)
    }
}
