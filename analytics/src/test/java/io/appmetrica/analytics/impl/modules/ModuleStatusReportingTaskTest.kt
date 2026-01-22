package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class ModuleStatusReportingTaskTest : CommonTest() {

    private val currentTime = System.currentTimeMillis()
    private val modulesType = "some_modules_type"

    private val executor = StubbedBlockingExecutor()
    private val preferences: SimplePreferenceStorage = mock {
        on { getString(any(), any()) } doReturn null
    }
    private val timeProvider: SystemTimeProvider = mock {
        on { currentTimeMillis() } doReturn currentTime
    }

    private val reporter: SelfReporterWrapper = mock()

    private val modulesStatus = listOf(
        ModuleStatus("module1", true),
        ModuleStatus("module2", false)
    )
    private val expectedJson = "{" +
        "\"modulesStatus\":[" +
        "{\"loaded\":true,\"moduleName\":\"module1\"}," +
        "{\"loaded\":false,\"moduleName\":\"module2\"}" +
        "]," +
        "\"lastSendTime\":$currentTime}"

    @get:Rule
    val appMetricaSelfReportFacadeRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn reporter
    }

    private val eventName = "${modulesType}_status"

    private val moduleStatusReportingTask by setUp {
        ModuleStatusReportingTask(
            preferences,
            modulesType,
            timeProvider,
            modulesStatus
        )
    }

    @Test
    fun reportModulesStatus() {
        val event = moduleStatusReportingTask.get()
        ObjectPropertyAssertions(event)
            .checkField("eventName", eventName)
            .checkField("eventValue", expectedJson)
            .checkAll()
        verify(preferences).putString("SOME_MODULES_TYPE_STATUS", expectedJson)
    }

    @Test
    fun reportModulesStatusIfTimeoutNotPassed() {
        val jsonFromPreferences = "{" +
            "\"modulesStatus\":[" +
            "{\"loaded\":true,\"moduleName\":\"module1\"}," +
            "{\"loaded\":false,\"moduleName\":\"module2\"}" +
            "]," +
            "\"lastSendTime\":${currentTime - TimeUnit.HOURS.toMillis(1)}}"
        whenever(preferences.getString("SOME_MODULES_TYPE_STATUS", null))
            .thenReturn(jsonFromPreferences)
        val event = moduleStatusReportingTask.get()
        assertThat(event).isNull()
        verify(preferences, never()).putString(eq("SOME_MODULES_TYPE_STATUS"), any())
    }

    @Test
    fun reportModulesStatusIfTimeoutNotPassedAndModulesChangedOrder() {
        val jsonFromPreferences = "{" +
            "\"modulesStatus\":[" +
            "{\"loaded\":true,\"moduleName\":\"module1\"}," +
            "{\"loaded\":false,\"moduleName\":\"module2\"}" +
            "]," +
            "\"lastSendTime\":${currentTime - TimeUnit.HOURS.toMillis(1)}}"
        whenever(preferences.getString("SOME_MODULES_TYPE_STATUS", null))
            .thenReturn(jsonFromPreferences)

        val task = ModuleStatusReportingTask(
            preferences,
            modulesType,
            timeProvider,
            listOf(
                ModuleStatus("module2", false),
                ModuleStatus("module1", true)
            )
        )
        val event = task.get()
        assertThat(event).isNull()
        verify(preferences, never()).putString(eq("SOME_MODULES_TYPE_STATUS"), any())
    }

    @Test
    fun reportModulesStatusIfTimeoutPassed() {
        val jsonFromPreferences = "{" +
            "\"modulesStatus\":[" +
            "{\"loaded\":true,\"moduleName\":\"module1\"}," +
            "{\"loaded\":false,\"moduleName\":\"module2\"}" +
            "]," +
            "\"lastSendTime\":${currentTime - TimeUnit.HOURS.toMillis(25)}}"
        whenever(preferences.getString("SOME_MODULES_TYPE_STATUS", null))
            .thenReturn(jsonFromPreferences)

        val event = moduleStatusReportingTask.get()
        ObjectPropertyAssertions(event)
            .checkField("eventName", eventName)
            .checkField("eventValue", expectedJson)
            .checkAll()
        verify(preferences).putString("SOME_MODULES_TYPE_STATUS", expectedJson)
    }

    @Test
    fun reportModulesStatusIfTimeoutNotPassedAndModulesChanged() {
        val jsonFromPreferences = "{" +
            "\"modulesStatus\":[" +
            "{\"loaded\":true,\"moduleName\":\"module1\"}" +
            "]," +
            "\"lastSendTime\":${currentTime - TimeUnit.HOURS.toMillis(1)}}"
        whenever(preferences.getString("SOME_MODULES_TYPE_STATUS", null))
            .thenReturn(jsonFromPreferences)
        val event = moduleStatusReportingTask.get()

        ObjectPropertyAssertions(event)
            .checkField("eventName", eventName)
            .checkField("eventValue", expectedJson)
            .checkAll()
        verify(preferences).putString("SOME_MODULES_TYPE_STATUS", expectedJson)
    }

    @Test
    fun reportModulesStatusIfTimeoutNotPassedAndModulesStatusesChanged() {
        val jsonFromPreferences = "{" +
            "\"modulesStatus\":[" +
            "{\"loaded\":false,\"moduleName\":\"module1\"}," +
            "{\"loaded\":false,\"moduleName\":\"module2\"}" +
            "]," +
            "\"lastSendTime\":${currentTime - TimeUnit.HOURS.toMillis(1)}}"
        whenever(preferences.getString("SOME_MODULES_TYPE_STATUS", null))
            .thenReturn(jsonFromPreferences)

        val event = moduleStatusReportingTask.get()

        ObjectPropertyAssertions(event)
            .checkField("eventName", eventName)
            .checkField("eventValue", expectedJson)
            .checkAll()

        verify(preferences).putString("SOME_MODULES_TYPE_STATUS", expectedJson)
    }
}
