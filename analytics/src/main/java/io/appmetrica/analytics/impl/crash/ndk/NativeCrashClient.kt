package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientModuleDummy

class NativeCrashClient(private val processConfiguration: ProcessConfiguration) {
    private val tag = "[NativeCrashClient]"

    private val clientModule: NativeCrashClientModule =
        ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashClientModule>(
            "io.appmetrica.analytics.ndkcrashes.NativeCrashClientModuleImpl"
        ) ?: NativeCrashClientModuleDummy()
    private val metadataSerializer = AppMetricaNativeCrashMetadataSerializer()

    private lateinit var nativeCrashMetadata: AppMetricaNativeCrashMetadata

    fun initHandling(context: Context, apiKey: String, errorEnvironment: String?) {
        DebugLogger.info(tag, "Start handling native crashes")
        nativeCrashMetadata = AppMetricaNativeCrashMetadata(
            apiKey,
            processConfiguration.packageName,
            CounterConfigurationReporterType.MAIN,
            processConfiguration.processID,
            processConfiguration.processSessionID,
            errorEnvironment
        )
        val nativeCrashFolder = FileUtils.getNativeCrashDirectory(context)?.absolutePath
        if (nativeCrashFolder == null) {
            DebugLogger.error(tag, "Skip handle native crash. Failed to get native crash folder")
            return
        }
        clientModule.initHandling(
            context,
            NativeCrashClientConfig(
                nativeCrashFolder = nativeCrashFolder,
                nativeCrashMetadata = metadataSerializer.serialize(nativeCrashMetadata),
            )
        )
    }

    fun updateErrorEnvironment(errorEnvironment: String?) {
        DebugLogger.info(tag, "Update error environment for native crashes. Env: $errorEnvironment")
        if (this::nativeCrashMetadata.isInitialized) {
            nativeCrashMetadata = nativeCrashMetadata.copy(errorEnvironment = errorEnvironment)
            clientModule.updateAppMetricaMetadata(metadataSerializer.serialize(nativeCrashMetadata))
        } else {
            DebugLogger.error(tag, "Skipp update error environment. nativeCrashMetadata is not initialized")
        }
    }
}
