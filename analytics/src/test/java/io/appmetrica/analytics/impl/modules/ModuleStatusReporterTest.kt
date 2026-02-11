package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class ModuleStatusReporterTest : CommonTest() {

    private val currentTime = System.currentTimeMillis()
    private val modulesType = "some_modules_type"

    private val executor: IHandlerExecutor = mock()
    private val preferences: SimplePreferenceStorage = mock {
        on { getString(any(), anyOrNull()) } doReturn null
    }
    private val timeProvider: SystemTimeProvider = mock {
        on { currentTimeMillis() } doReturn currentTime
    }

    private val reporter: SelfReporterWrapper = mock()

    private val modulesStatus = listOf(
        ModuleStatus("module1", true),
        ModuleStatus("module2", false)
    )

    @get:Rule
    val appMetricaSelfReportFacadeRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn reporter
    }

    @get:Rule
    val moduleStatusReportingTaskRule = constructionRule<ModuleStatusReportingTask>()

    private val runnableCaptor = argumentCaptor<Runnable>()

    private val moduleStatusReporter = ModuleStatusReporter(
        executor,
        preferences,
        modulesType,
        timeProvider
    )

    @Test
    fun reportModuleStatus() {
        moduleStatusReporter.reportModulesStatus(modulesStatus)
        verifyNoInteractions(reporter)
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()
        verify(reporter)
            .reportLazyEvent(moduleStatusReportingTaskRule.constructionMock.constructed().first())

        assertThat(moduleStatusReportingTaskRule.constructionMock.constructed()).hasSize(1)
        assertThat(moduleStatusReportingTaskRule.argumentInterceptor.flatArguments())
            .containsExactly(preferences, modulesType, timeProvider, modulesStatus)
    }
}
