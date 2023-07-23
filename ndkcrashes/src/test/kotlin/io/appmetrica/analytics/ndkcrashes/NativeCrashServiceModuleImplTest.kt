package io.appmetrica.analytics.ndkcrashes

import android.content.Context
import io.appmetrica.analytics.ndkcrashes.impl.NativeCrashWatcher
import io.appmetrica.analytics.ndkcrashes.impl.NativeLibraryLoader
import io.appmetrica.analytics.ndkcrashes.jni.service.CrashpadCrash
import io.appmetrica.analytics.ndkcrashes.jni.service.NativeCrashServiceJniWrapper
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.getValue
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeCrashServiceModuleImplTest : CommonTest() {
    companion object {
        private const val NATIVE_CRASH_DIR = "nativeCrashDir"
        private const val PACKAGE_NAME = "package"
        private const val SOCKET_NAME = "$PACKAGE_NAME-appmetrica-crashpad_socket"
    }

    @get:Rule
    internal val libraryLoaderMockedConstructionRule = constructionRule<NativeLibraryLoader.LibraryLoader> {
        on { load("appmetrica-service-native") } doAnswer {
            if (!isLibraryLoadedSuccess) throw RuntimeException()
        }
    }
    private var isLibraryLoadedSuccess: Boolean = true

    @get:Rule
    internal val nativeCrashServiceJniWrapperMockedRule = staticRule<NativeCrashServiceJniWrapper>()
    private val nativeCrashServiceJniWrapperStaticMock by nativeCrashServiceJniWrapperMockedRule

    @get:Rule
    val nativeCrashWatcherMockedConstructionRule = constructionRule<NativeCrashWatcher>()
    private val nativeCrashWatcherConstructorArgs by nativeCrashWatcherMockedConstructionRule.argumentInterceptor
    private val nativeCrashWatcher by nativeCrashWatcherMockedConstructionRule

    private val context = mock<Context> {
        on { packageName } doReturn PACKAGE_NAME
    }
    private val config = mock<NativeCrashServiceConfig> {
        on { nativeCrashFolder } doReturn NATIVE_CRASH_DIR
    }

    private val module by setUp { NativeCrashServiceModuleImpl() }

    @Test
    fun init() {
        module.init(context, config)
        nativeCrashServiceJniWrapperStaticMock.verify {
            NativeCrashServiceJniWrapper.init(eq(NATIVE_CRASH_DIR))
        }
        verify(nativeCrashWatcher).subscribe(any())
        assertThat(nativeCrashWatcherConstructorArgs.first()).isEqualTo(SOCKET_NAME)
    }

    @Test
    fun `init with failed load library`() {
        isLibraryLoadedSuccess = false
        module.init(context, config)
        nativeCrashServiceJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun getAllCrashes() {
        val crashpadCrashes = List(3) { mockCrashpadCrash(it) }
        whenever(NativeCrashServiceJniWrapper.readAllCrashes()).thenReturn(crashpadCrashes)
        val crashes = module.getAllCrashes()
        for ((index, crash) in crashes.withIndex()) {
            checkNativeCrash(crash, index)
        }
    }

    @Test
    fun `getAllCrashes with failed load library`() {
        isLibraryLoadedSuccess = false
        module.getAllCrashes()
        nativeCrashServiceJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun markCrashCompleted() {
        val uuid = "crash-uuid"
        module.markCrashCompleted(uuid)
        nativeCrashServiceJniWrapperStaticMock.verify {
            NativeCrashServiceJniWrapper.markCrashCompleted(eq(uuid))
        }
    }

    @Test
    fun `markCrashCompleted with failed load library`() {
        isLibraryLoadedSuccess = false
        module.markCrashCompleted("uuid")
        nativeCrashServiceJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun deleteCompletedCrashes() {
        module.deleteCompletedCrashes()
        nativeCrashServiceJniWrapperStaticMock.verify {
            NativeCrashServiceJniWrapper.deleteCompletedCrashes()
        }
    }

    @Test
    fun `deleteCompletedCrashes with failed load library`() {
        isLibraryLoadedSuccess = false
        module.deleteCompletedCrashes()
        nativeCrashServiceJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun `nativeCrashListener if nativeCrashHandler not set`() {
        val listener = getNativeCrashListener()
        val uuid = "crash-uuid"
        listener.onNewCrash(uuid)
        nativeCrashServiceJniWrapperStaticMock.verifyNoInteractions()
    }

    @Test
    fun `nativeCrashListener with failed read crash`() {
        val uuid = "crash-uuid"
        whenever(NativeCrashServiceJniWrapper.readCrash(uuid)).thenThrow(RuntimeException())

        val handler = mock<NativeCrashHandler>()
        module.setDefaultCrashHandler(handler)

        val listener = getNativeCrashListener()
        listener.onNewCrash(uuid)

        verifyNoMoreInteractions(handler)
    }

    @Test
    fun `nativeCrashListener with nativeCrashHandler`() {
        val uuid = "crash-uuid"
        val crashpadCrash = mockCrashpadCrash()
        whenever(NativeCrashServiceJniWrapper.readCrash(uuid)).thenReturn(crashpadCrash)

        val listener = getNativeCrashListener()
        val handler = mock<NativeCrashHandler>()
        module.setDefaultCrashHandler(handler)

        listener.onNewCrash(uuid)

        val crashCaptor = argumentCaptor<NativeCrash>()
        verify(handler).newCrash(crashCaptor.capture())
        checkNativeCrash(crashCaptor.firstValue)
    }

    private fun mockCrashpadCrash(number: Int = 10) = mock<CrashpadCrash> {
        on { uuid } doReturn "uuid$number"
        on { dumpFile } doReturn "dumpFile$number"
        on { creationTime } doReturn number * 1000L
        on { appMetricaData } doReturn "metadata$number"
    }

    private fun checkNativeCrash(crash: NativeCrash, number: Int = 10) {
        assertThat(crash.source).isEqualTo(NativeCrashSource.CRASHPAD)
        assertThat(crash.handlerVersion).isEqualTo(BuildConfig.VERSION_NAME)
        assertThat(crash.uuid).isEqualTo("uuid$number")
        assertThat(crash.dumpFile).isEqualTo("dumpFile$number")
        assertThat(crash.creationTime).isEqualTo(number * 1000L)
        assertThat(crash.metadata).isEqualTo("metadata$number")
    }

    private fun getNativeCrashListener(): NativeCrashWatcher.Listener {
        module.init(context, config)
        val listenerCaptor = argumentCaptor<NativeCrashWatcher.Listener>()
        verify(nativeCrashWatcher).subscribe(listenerCaptor.capture())
        nativeCrashServiceJniWrapperStaticMock.clearInvocations()
        return listenerCaptor.firstValue
    }
}
