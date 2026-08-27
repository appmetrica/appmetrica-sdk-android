package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Handler
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController
import io.appmetrica.analytics.impl.modules.ModuleStatus
import io.appmetrica.analytics.impl.modules.ModuleStatusReporter
import io.appmetrica.analytics.impl.modules.ModulesSeeker
import io.appmetrica.analytics.impl.modules.client.context.ClientContextImpl
import io.appmetrica.analytics.impl.modules.plugin.PluginModuleStatusDetector
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// Covers the case where AppMetricaImpl is instantiated in the main process but no client modules
// are discovered (e.g. a plain SDK build without optional modules). Module status must still be
// reported — containing only the plugin entries — because the guard is isMainProcess(), not
// status.isNotEmpty().
// A separate test class is needed because modulesSeekerConstructionRule is a @Rule that cannot
// be overridden per-test within AppMetricaImplTest.
internal class AppMetricaImplMainProcessEmptyModulesTest : CommonTest() {

    private val context: Context = mock()

    private val defaultHandler: Handler = mock()
    private val defaultExecutor: ICommonExecutor = mock()
    private val appOpenWatcher: AppOpenWatcher = mock()
    private val jvmCrashClientController: JvmCrashClientController = mock()

    private val appmetricaCore: IAppMetricaCore = mock {
        on { defaultHandler } doReturn defaultHandler
        on { defaultExecutor } doReturn defaultExecutor
        on { appOpenWatcher } doReturn appOpenWatcher
        on { jvmCrashClientController } doReturn jvmCrashClientController
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    // discoverClientModules() returns empty list — no client modules registered
    @get:Rule
    val modulesSeekerConstructionRule = constructionRule<ModulesSeeker> {
        on { discoverClientModules() } doReturn emptyList()
    }

    private val pluginModulesStatus = listOf(mock<ModuleStatus>())

    @get:Rule
    val pluginModuleStatusDetectorConstructionRule = constructionRule<PluginModuleStatusDetector> {
        on { detect() } doReturn pluginModulesStatus
    }

    @get:Rule
    val clientContextImplConstructionRule = constructionRule<ClientContextImpl>()

    private val moduleStatusReporter: ModuleStatusReporter = mock()

    @get:Rule
    val fieldsProviderConstructionRule = constructionRule<AppMetricaImplFieldsProvider> {
        on { createDataResultReceiver(any(), any()) } doReturn mock()
        on { createProcessConfiguration(any(), any()) } doReturn mock()
        on { createReportsHandler(any(), any(), any()) } doReturn mock()
        on { createStartupHelper(any(), any(), any()) } doReturn mock()
        on { createReferrerHelper(any(), any(), any()) } doReturn mock()
        on { createReporterFactory(any(), any(), any(), any(), any()) } doReturn mock()
        on { createModuleStatusReporter(context) } doReturn moduleStatusReporter
    }

    @Test
    fun `constructor reports module status in main process even if no client modules discovered`() {
        whenever(clientServiceLocatorRule.currentProcessDetector.isMainProcess()).thenReturn(true)
        AppMetricaImpl(context, appmetricaCore)
        // status is empty, but plugin entries are still reported
        verify(moduleStatusReporter).reportModulesStatus(pluginModulesStatus)
    }
}
