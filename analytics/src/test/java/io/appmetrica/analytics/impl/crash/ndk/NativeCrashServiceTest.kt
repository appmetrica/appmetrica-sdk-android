package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ndk.service.NativeCrashHandlerFactory
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModuleDummy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.mockFile
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeCrashServiceTest : CommonTest() {
    companion object {
        private const val MODULE_CLASS = "io.appmetrica.analytics.ndkcrashes.NativeCrashServiceModuleImpl"
    }

    private val context = mock<Context>()
    private val reportConsumer = mock<ReportConsumer>()
    private val moduleService = mock<NativeCrashServiceModule>()
    private val path = "path"
    private val nativeCrashDir = mockFile(path)
    private val uuid = "Uuid"

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
    val nativeCrashServiceModuleDummyMockedConstructionRule = constructionRule<NativeCrashServiceModuleDummy>()
    private val nativeCrashServiceModuleDummy: NativeCrashServiceModuleDummy
        by nativeCrashServiceModuleDummyMockedConstructionRule

    private val nativeCrashHandlerForActualSession: NativeCrashHandler = mock()
    private val nativeCrashHandlerForPrevSession: NativeCrashHandler = mock()

    @get:Rule
    val nativeCrashHandlerFactoryMockedConstructionRule = constructionRule<NativeCrashHandlerFactory> {
        on { createHandlerForActualSession(context, reportConsumer) } doReturn nativeCrashHandlerForActualSession
        on { createHandlerForPrevSession(context, reportConsumer) } doReturn nativeCrashHandlerForPrevSession
    }
    private val nativeCrashHandlerFactory: NativeCrashHandlerFactory by nativeCrashHandlerFactoryMockedConstructionRule

    @get:Rule
    val nativeCrashServiceConfigMockedConstructionRule = constructionRule<NativeCrashServiceConfig>()
    private val nativeCrashServiceConfig: NativeCrashServiceConfig by nativeCrashServiceConfigMockedConstructionRule

    @Test
    fun `initNativeCrashReporting without module client`() {
        whenever(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(MODULE_CLASS))
            .thenReturn(null)

        createService().initNativeCrashReporting(context, reportConsumer)

        assertThat(nativeCrashServiceModuleDummyMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeCrashServiceModuleDummyMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        verify(nativeCrashServiceModuleDummy).init(context, nativeCrashServiceConfig)
        verify(nativeCrashServiceModuleDummy).setDefaultCrashHandler(nativeCrashHandlerForActualSession)
        verify(nativeCrashServiceModuleDummy).getAllCrashes()
        verifyNoMoreInteractions(nativeCrashServiceModuleDummy)
    }

    @Test
    fun `initNativeCrashReporting without module client and crash directory`() {
        whenever(FileUtils.getNativeCrashDirectory(context)).thenReturn(null)
        whenever(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(MODULE_CLASS))
            .thenReturn(null)

        createService().initNativeCrashReporting(context, reportConsumer)
        verifyNoInteractions(nativeCrashServiceModuleDummy)
    }

    @Test
    fun `check completion`() {
        createService()
        assertThat(nativeCrashHandlerFactoryMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        val completion =
            nativeCrashHandlerFactoryMockedConstructionRule.argumentInterceptor.flatArguments().first()
                as (String) -> Unit
        completion.invoke(uuid)
        verify(moduleService).markCrashCompleted(uuid)
        verify(moduleService).deleteCompletedCrashes()
    }

    @Test
    fun initNativeCrashReporting() {
        val firstNativeCrash: NativeCrash = mock()
        val secondNativeCrash: NativeCrash = mock()
        whenever(moduleService.getAllCrashes()).thenReturn(listOf(firstNativeCrash, secondNativeCrash))

        val service = createService()
        service.initNativeCrashReporting(context, reportConsumer)

        verify(moduleService).init(context, nativeCrashServiceConfig)
        verify(moduleService).setDefaultCrashHandler(nativeCrashHandlerForActualSession)
        verify(nativeCrashHandlerForPrevSession).newCrash(firstNativeCrash)
        verify(nativeCrashHandlerForPrevSession).newCrash(secondNativeCrash)

        assertThat(nativeCrashServiceConfigMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(nativeCrashServiceConfigMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(path)
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
