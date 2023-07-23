package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.CounterConfigurationReporterType
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientModuleDummy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeCrashClientTest : CommonTest() {
    companion object {
        private const val MODULE_CLASS = "io.appmetrica.analytics.ndkcrashes.NativeCrashClientModuleImpl"
        private const val API_KEY = "apiKey"
        private const val PACKAGE_NAME = "packageName"
        private const val PROCESS_ID = 0
        private const val PROCESS_SESSION_ID = "0"
        private const val ERROR_ENV = "errorEnv"
        private const val METADATA = "metadata"
    }

    @get:Rule
    val reflectiveUtilsMockedRule = staticRule<ReflectionUtils> {
        on {
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashClientModule>(MODULE_CLASS)
        } doReturn moduleClient
    }

    @get:Rule
    val fileUtilsMockedRule = staticRule<FileUtils> {
        on { FileUtils.getNativeCrashDirectory(context) } doReturn nativeCrashDir
    }

    @get:Rule
    val metadataSerializerMockedConstructionRule = constructionRule<AppMetricaNativeCrashMetadataSerializer> {
        on { serialize(any()) } doReturn METADATA
    }
    private val metadataSerializer by metadataSerializerMockedConstructionRule

    private val context = mock<Context>()
    private val moduleClient = mock<NativeCrashClientModule>()
    private val processConfiguration = mock<ProcessConfiguration> {
        on { packageName } doReturn PACKAGE_NAME
        on { processID } doReturn PROCESS_ID
        on { processSessionID } doReturn PROCESS_SESSION_ID
    }
    private val nativeCrashDir = mockFile("path")

    @Test
    fun `without module client`() {
        whenever(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashClientModule>(MODULE_CLASS))
            .thenReturn(null)
        val dummyConstructor = mockConstruction(NativeCrashClientModuleDummy::class.java)

        createClient()

        assertThat(dummyConstructor.constructed()).hasSize(1)
        dummyConstructor.close()
    }

    @Test
    fun initHandling() {
        val client = createClient()
        client.initHandling(context, API_KEY, ERROR_ENV)

        val configCaptor = argumentCaptor<NativeCrashClientConfig>()
        val metadataCaptor = argumentCaptor<AppMetricaNativeCrashMetadata>()

        verify(moduleClient).initHandling(eq(context), configCaptor.capture())
        verify(metadataSerializer).serialize(metadataCaptor.capture())

        with(configCaptor.firstValue) {
            assertThat(nativeCrashFolder).isEqualTo(nativeCrashDir.absolutePath)
            assertThat(nativeCrashMetadata).isEqualTo(METADATA)
        }
        checkCrashMetadata(metadataCaptor.firstValue)
    }

    @Test
    fun `initHandling without native crash dir`() {
        whenever(FileUtils.getNativeCrashDirectory(context)).thenReturn(null)

        val client = createClient()
        client.initHandling(context, API_KEY, ERROR_ENV)

        verify(moduleClient, never()).initHandling(any(), any())
    }

    @Test
    fun updateErrorEnvironment() {
        val client = NativeCrashClient(processConfiguration)
        client.initHandling(context, API_KEY, ERROR_ENV)

        for ((checkNumber, errEnv) in listOf("otherErrEnv", "!@#$$", "", null).withIndex()) {
            client.updateErrorEnvironment(errEnv)

            val metadataCaptor = argumentCaptor<AppMetricaNativeCrashMetadata>()

            verify(moduleClient, times(checkNumber + 1)).updateAppMetricaMetadata(METADATA)
            verify(metadataSerializer, times(checkNumber + 2)).serialize(metadataCaptor.capture())

            checkCrashMetadata(metadataCaptor.lastValue, errorEnv = errEnv)
        }
    }

    @Test
    fun `updateErrorEnvironment without init`() {
        val client = NativeCrashClient(processConfiguration)
        client.updateErrorEnvironment("err env")

        verify(moduleClient, never()).updateAppMetricaMetadata(any())
    }

    private fun createClient() = NativeCrashClient(processConfiguration)

    private fun checkCrashMetadata(metadata: AppMetricaNativeCrashMetadata, errorEnv: String? = ERROR_ENV) {
        assertThat(metadata.apiKey).isEqualTo(API_KEY)
        assertThat(metadata.packageName).isEqualTo(PACKAGE_NAME)
        assertThat(metadata.reporterType).isEqualTo(CounterConfigurationReporterType.MAIN)
        assertThat(metadata.processID).isEqualTo(PROCESS_ID)
        assertThat(metadata.processSessionID).isEqualTo(PROCESS_SESSION_ID)
        assertThat(metadata.errorEnvironment).isEqualTo(errorEnv)
    }
}
