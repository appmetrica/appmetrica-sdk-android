package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context
import io.appmetrica.analytics.logger.internal.YLogger

class NativeCrashClientModuleDummy : NativeCrashClientModule() {
    private val tag = "[NativeCrashClientModuleDummy]"

    override fun initHandling(context: Context, config: NativeCrashClientConfig) {
        YLogger.debug(tag, "Skipping native crashes handling")
    }

    override fun updateAppMetricaMetadata(metadata: String) {
        YLogger.debug(tag, "Skipping update native crash metadata")
    }
}
