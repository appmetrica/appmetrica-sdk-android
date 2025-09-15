package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class ModuleStatusReporterTest : CommonTest() {

    private val currentTime = System.currentTimeMillis()
    private val modulesType = "some_modules_type"

    private val executor = StubbedBlockingExecutor()
    private val preferences: SimplePreferenceStorage = mock {
        on { getString(any(), any()) } doReturn null
    }
    private val timeProvider: SystemTimeProvider = mock {
        on { currentTimeMillis() } doReturn currentTime
    }

    private val reporter: IReporterExtended = mock()

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

    private val moduleStatusReporter = ModuleStatusReporter(
        executor,
        preferences,
        modulesType,
        timeProvider
    )

    @Test
    fun reportModulesStatus() {
        moduleStatusReporter.reportModulesStatus(modulesStatus)

        verify(reporter).reportEvent(
            "${modulesType}_status",
            expectedJson
        )
        verify(preferences).getString("SOME_MODULES_TYPE_STATUS", null)
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

        moduleStatusReporter.reportModulesStatus(modulesStatus)

        verify(preferences).getString("SOME_MODULES_TYPE_STATUS", null)
        verifyNoInteractions(reporter)
        verifyNoMoreInteractions(preferences)
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

        moduleStatusReporter.reportModulesStatus(modulesStatus)

        verify(reporter).reportEvent(
            "${modulesType}_status",
            expectedJson
        )
        verify(preferences).getString("SOME_MODULES_TYPE_STATUS", null)
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

        moduleStatusReporter.reportModulesStatus(modulesStatus)

        verify(reporter).reportEvent(
            "${modulesType}_status",
            expectedJson
        )
        verify(preferences).getString("SOME_MODULES_TYPE_STATUS", null)
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

        moduleStatusReporter.reportModulesStatus(modulesStatus)

        verify(reporter).reportEvent(
            "${modulesType}_status",
            expectedJson
        )
        verify(preferences).getString("SOME_MODULES_TYPE_STATUS", null)
        verify(preferences).putString("SOME_MODULES_TYPE_STATUS", expectedJson)
    }
}
