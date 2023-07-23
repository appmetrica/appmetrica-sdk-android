package io.appmetrica.analytics.ndkcrashes

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import io.appmetrica.analytics.ndkcrashes.impl.CrashpadHandlerFinder
import io.appmetrica.analytics.ndkcrashes.impl.NativeLibraryLoader
import io.appmetrica.analytics.ndkcrashes.impl.utils.AbiResolver
import io.appmetrica.analytics.ndkcrashes.impl.utils.PackagePaths
import io.appmetrica.analytics.ndkcrashes.jni.core.NativeCrashCoreJniWrapper
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.mockFile
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NativeCrashClientModuleImplTest : CommonTest() {
    companion object {
        private const val HANDLER_PATH = "handlerPath"
        private const val NATIVE_CRASH_DIR = "nativeCrashDir"
        private const val APPMETRICA_METADATA = "metadata"
        private const val PACKAGE_NAME = "package"
        private const val SOCKET_NAME = "$PACKAGE_NAME-appmetrica-crashpad_socket"
        private const val APK_PATH = "apk-path"
        private const val LIB_PATH = "lib-path"
        private const val DATA_DIR = "data-dir"
    }

    @get:Rule
    internal val libraryLoaderMockedConstructionRule = constructionRule<NativeLibraryLoader.LibraryLoader> {
        on { load("appmetrica-native") } doAnswer {
            if (!isLibraryLoadedSuccess) throw RuntimeException()
        }
    }
    private var isLibraryLoadedSuccess: Boolean = true

    @get:Rule
    val crashpadHandlerFinderMockedConstructionRule = constructionRule<CrashpadHandlerFinder> {
        val path = mockFile(HANDLER_PATH)
        on { find(context) } doReturn path
    }

    @get:Rule
    internal val nativeCrashCoreJniWrapperMockedRule = staticRule<NativeCrashCoreJniWrapper> {
        on { NativeCrashCoreJniWrapper.startHandlerWithLinkerAtCrash(any(), any(), any(), any(), any()) } doReturn true
        on { NativeCrashCoreJniWrapper.startHandlerAtCrash(any(), any(), any(), any()) } doReturn true
        on {
            NativeCrashCoreJniWrapper.startJavaHandlerAtCrash(any(), any(), any(), any(), any(), any(), any(), any())
        } doReturn true
    }
    private val nativeCrashCoreJniWrapperStaticMock by nativeCrashCoreJniWrapperMockedRule

    @get:Rule
    val packagePathsMockedRule = staticRule<PackagePaths> {
        on { PackagePaths.makePackagePaths(context, AbiResolver.getAbi()!!) } doReturn arrayOf(APK_PATH, LIB_PATH)
    }

    @get:Rule
    val environmentMockedRule = staticRule<Environment> {
        val dir = mockFile(DATA_DIR)
        on { Environment.getDataDirectory() } doReturn dir
    }

    private val context = mock<Context> {
        on { packageName } doReturn PACKAGE_NAME
    }
    private val config = mock<NativeCrashClientConfig> {
        on { nativeCrashFolder } doReturn NATIVE_CRASH_DIR
        on { nativeCrashMetadata } doReturn APPMETRICA_METADATA
    }

    private val module by setUp { NativeCrashClientModuleImpl() }

    @Test
    fun `initHandling with failed load library`() {
        isLibraryLoadedSuccess = false
        module.initHandling(context, config)
        nativeCrashCoreJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `initHandling on Q+`() {
        module.initHandling(context, config)

        nativeCrashCoreJniWrapperStaticMock.verify {
            NativeCrashCoreJniWrapper.startHandlerWithLinkerAtCrash(
                eq(HANDLER_PATH),
                eq(NATIVE_CRASH_DIR),
                eq(SOCKET_NAME),
                eq(Process.is64Bit()),
                eq(APPMETRICA_METADATA),
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `initHandling on Q+ failed start handler`() {
        whenever(NativeCrashCoreJniWrapper.startHandlerWithLinkerAtCrash(any(), any(), any(), any(), any()))
            .thenReturn(false)

        module.initHandling(context, config)
        // without crash
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `initHandling on Q+ with crash start handler`() {
        whenever(NativeCrashCoreJniWrapper.startHandlerWithLinkerAtCrash(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException())

        module.initHandling(context, config)
        // without crash
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `initHandling on M+`() {
        module.initHandling(context, config)

        nativeCrashCoreJniWrapperStaticMock.verify {
            NativeCrashCoreJniWrapper.startJavaHandlerAtCrash(
                eq("io.appmetrica.analytics.ndkcrashes.JavaHandlerRunner"),
                eq(HANDLER_PATH),
                eq(NATIVE_CRASH_DIR),
                eq(SOCKET_NAME),
                eq(APK_PATH),
                eq(LIB_PATH),
                eq(DATA_DIR),
                eq(APPMETRICA_METADATA),
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `initHandling on M+ failed start handler`() {
        whenever(
            NativeCrashCoreJniWrapper.startJavaHandlerAtCrash(any(), any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(false)

        module.initHandling(context, config)
        // without crash
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun `initHandling on M+ with crash start handler`() {
        whenever(
            NativeCrashCoreJniWrapper.startJavaHandlerAtCrash(any(), any(), any(), any(), any(), any(), any(), any())
        ).thenThrow(RuntimeException())

        module.initHandling(context, config)
        // without crash
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `initHandling on LOLLIPOP_MR1+`() {
        module.initHandling(context, config)

        nativeCrashCoreJniWrapperStaticMock.verify {
            NativeCrashCoreJniWrapper.startHandlerAtCrash(
                eq(HANDLER_PATH),
                eq(NATIVE_CRASH_DIR),
                eq(SOCKET_NAME),
                eq(APPMETRICA_METADATA),
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `initHandling on LOLLIPOP_MR1+ failed start handler`() {
        whenever(NativeCrashCoreJniWrapper.startHandlerAtCrash(any(), any(), any(), any()))
            .thenReturn(false)

        module.initHandling(context, config)
        // without crash
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP_MR1])
    fun `initHandling on LOLLIPOP_MR1+ with crash start handler`() {
        whenever(NativeCrashCoreJniWrapper.startHandlerAtCrash(any(), any(), any(), any()))
            .thenThrow(RuntimeException())

        module.initHandling(context, config)
        // without crash
    }

    @Test
    fun `updateAppMetricaMetadata with failed load library`() {
        isLibraryLoadedSuccess = false
        module.updateAppMetricaMetadata(APPMETRICA_METADATA)

        nativeCrashCoreJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun updateAppMetricaMetadata() {
        module.updateAppMetricaMetadata(APPMETRICA_METADATA)

        nativeCrashCoreJniWrapperStaticMock.verify {
            NativeCrashCoreJniWrapper.updateAppMetricaMetadata(eq(APPMETRICA_METADATA))
        }
    }
}
