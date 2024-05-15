package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context
import io.appmetrica.analytics.logger.internal.DebugLogger

class NativeCrashClientModuleDummy : NativeCrashClientModule() {
    private val tag = "[NativeCrashClientModuleDummy]"

    override fun initHandling(context: Context, config: NativeCrashClientConfig) {
        DebugLogger.info(tag, "Skipping native crashes handling")
    }

    override fun updateAppMetricaMetadata(metadata: String) {
        DebugLogger.info(tag, "Skipping update native crash metadata")
    }
}
