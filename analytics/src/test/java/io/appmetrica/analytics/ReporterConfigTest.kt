package io.appmetrica.analytics

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.proxy.validation.ConfigChecker
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.MockedConstruction.MockInitializer
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ReporterConfigTest : CommonTest() {
    private val oldMaxReportsInDatabaseCount = 10
    private val newMaxReportsInDatabaseCount = 100

    @get:Rule
    val configCheckerRule = MockedConstructionRule(
        ConfigChecker::class.java,
        MockInitializer { mock, _ ->
            whenever(mock.getCheckedMaxReportsInDatabaseCount(ArgumentMatchers.anyInt())).thenAnswer(
                Answer { invocation -> invocation.getArgument<Int>(0) }
            )
            whenever(mock.getCheckedMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount))
                .thenReturn(newMaxReportsInDatabaseCount)
        }
    )
    private val additionalConfigKeyFirst = "key1"
    private val additionalConfigValueFirst = "value1"
    private val additionalConfigKeySecond = "key2"
    private val additionalConfigValueSecond = "value2"
    
    private val additionalConfigMap = mapOf(
        additionalConfigKeyFirst to additionalConfigValueFirst,
        additionalConfigKeySecond to additionalConfigValueSecond
    )

    private val mApiKey = UUID.randomUUID().toString()
    private val appEnvironmentMapKeyFirst = "appEnvironmentMap_key1"
    private val appEnvironmentMapValueFirst = "appEnvironmentMap_value1"
    private val appEnvironmentMapKeySecond = "appEnvironmentMap_key2"
    private val appEnvironmentMapValueSecond = "appEnvironmentMap_value2"
    
    private val appEnvironmentMap = mapOf(
        appEnvironmentMapKeyFirst to appEnvironmentMapValueFirst,
        appEnvironmentMapKeySecond to appEnvironmentMapValueSecond
    )

    @Test
    fun builder() {
        val config = ReporterConfig.newConfigBuilder(mApiKey)
            .withSessionTimeout(SESSION_TIMEOUT)
            .withLogs()
            .withDataSendingEnabled(DATA_SENDING_ENABLED)
            .withMaxReportsInDatabaseCount(MAX_REPORTS_IN_DB_COUNT)
            .withUserProfileID(USER_PROFILE_ID)
            .withDispatchPeriodSeconds(DISPATCH_PERIOD)
            .withMaxReportsCount(MAX_REPORTS_COUNT)
            .withAppEnvironmentValue(appEnvironmentMapKeyFirst, appEnvironmentMapValueFirst)
            .withAppEnvironmentValue(appEnvironmentMapKeySecond, appEnvironmentMapValueSecond)
            .withAdditionalConfig(additionalConfigKeyFirst, additionalConfigValueFirst)
            .withAdditionalConfig(additionalConfigKeySecond, additionalConfigValueSecond)
            .build()
        ObjectPropertyAssertions(config)
            .checkField("apiKey", mApiKey)
            .checkField("sessionTimeout", SESSION_TIMEOUT)
            .checkField("logs", true)
            .checkField("dataSendingEnabled", DATA_SENDING_ENABLED)
            .checkField("maxReportsInDatabaseCount", MAX_REPORTS_IN_DB_COUNT)
            .checkField("userProfileID", USER_PROFILE_ID)
            .checkField("dispatchPeriodSeconds", DISPATCH_PERIOD)
            .checkField("maxReportsCount", MAX_REPORTS_COUNT)
            .checkField("appEnvironment", appEnvironmentMap)
            .checkField("additionalConfig", additionalConfigMap)
            .checkAll()
    }

    @Test
    fun buildObjectWithDefaults() {
        val config = ReporterConfig.newConfigBuilder(mApiKey).build()
        ObjectPropertyAssertions(config)
            .checkField("apiKey", mApiKey)
            .checkField<Any>("sessionTimeout", null)
            .checkField<Any>("logs", null)
            .checkField<Any>("dataSendingEnabled", null)
            .checkField<Any>("maxReportsInDatabaseCount", null)
            .checkField<Any>("userProfileID", null)
            .checkField<Any>("dispatchPeriodSeconds", null)
            .checkField<Any>("maxReportsCount", null)
            .checkField("appEnvironment", emptyMap<Any, Any>())
            .checkField("additionalConfig", emptyMap<Any, Any>())
            .checkAll()
    }

    @Test(expected = ValidationException::class)
    fun invalidApiKey() {
        ReporterConfig.newConfigBuilder("")
    }

    @Test 
    fun testInvalidMaxReportsInDatabaseCount() {
        val config = ReporterConfig.newConfigBuilder(mApiKey)
            .withMaxReportsInDatabaseCount(oldMaxReportsInDatabaseCount)
            .build()
        assertThat(config.maxReportsInDatabaseCount).isEqualTo(newMaxReportsInDatabaseCount)
    }
    companion object {
        private const val SESSION_TIMEOUT = 44
        private const val DATA_SENDING_ENABLED = true
        private const val MAX_REPORTS_IN_DB_COUNT = 2000
        private const val USER_PROFILE_ID = "user_profile_id"
        private const val DISPATCH_PERIOD = 122
        private const val MAX_REPORTS_COUNT = 22
    }
}
