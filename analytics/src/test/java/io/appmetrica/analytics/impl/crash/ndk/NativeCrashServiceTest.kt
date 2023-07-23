package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModuleDummy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.getValue
import io.appmetrica.analytics.testutils.mockFile
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeCrashServiceTest : CommonTest() {
    companion object {
        private const val MODULE_CLASS = "io.appmetrica.analytics.ndkcrashes.NativeCrashServiceModuleImpl"
    }

    @get:Rule
    val reflectiveUtilsMockedRule = staticRule<ReflectionUtils> {
        on {
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(MODULE_CLASS)
        } doReturn moduleService
    }

    @get:Rule
    val fileUtilsMockedRule = staticRule<FileUtils> {
        on { FileUtils.getNativeCrashDirectory(context) } doReturn nativeCrashDir
    }

    @get:Rule
    val nativeCrashReporterMockedConstructionRule = constructionRule<NativeCrashReporter>()
    private val nativeCrashReporterConstructorArgs by nativeCrashReporterMockedConstructionRule.argumentInterceptor
    private val nativeCrashReporter by nativeCrashReporterMockedConstructionRule

    private val context = mock<Context>()
    private val reportConsumer = mock<ReportConsumer>()
    private val moduleService = mock<NativeCrashServiceModule>()
    private val nativeCrashDir = mockFile("path")

    @Test
    fun `without module client`() {
        whenever(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(MODULE_CLASS))
            .thenReturn(null)
        val dummyConstructor = mockConstruction(NativeCrashServiceModuleDummy::class.java)

        createService()

        assertThat(dummyConstructor.constructed()).hasSize(1)
        dummyConstructor.close()
    }

    @Test
    fun initNativeCrashReporting() {
        val crashes = listOf<NativeCrash>(mock(), mock())
        whenever(moduleService.getAllCrashes()).thenReturn(crashes)

        val service = createService()
        service.initNativeCrashReporting(context, reportConsumer)

        val configCaptor = argumentCaptor<NativeCrashServiceConfig>()
        verify(moduleService).init(eq(context), configCaptor.capture())
        verify(moduleService).setDefaultCrashHandler(refEq(nativeCrashReporter))
        verify(nativeCrashReporter).reportCrashesFromPrevSession(refEq(crashes))

        with(configCaptor.firstValue) {
            assertThat(nativeCrashFolder).isEqualTo(nativeCrashDir.absolutePath)
        }
        assertThat(nativeCrashReporterConstructorArgs[0]).isEqualTo(reportConsumer)
    }

    @Test
    fun markCrashCompletedAndDeleteCompletedCrashes() {
        val service = createService()
        service.initNativeCrashReporting(context, reportConsumer)

        @Suppress("UNCHECKED_CAST")
        val testFunc = nativeCrashReporterConstructorArgs[1] as (String) -> Unit
        val uuid = "uuid"
        testFunc(uuid)

        verify(moduleService).markCrashCompleted(eq(uuid))
        verify(moduleService).deleteCompletedCrashes()
    }

    @Test
    fun `initNativeCrashReporting without native crash dir`() {
        whenever(FileUtils.getNativeCrashDirectory(context)).thenReturn(null)

        val client = createService()
        client.initNativeCrashReporting(context, reportConsumer)

        verify(moduleService, never()).init(any(), any())
        verify(moduleService, never()).setDefaultCrashHandler(any())
    }

    private fun createService() = NativeCrashService()
}
