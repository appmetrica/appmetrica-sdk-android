package io.appmetrica.analytics.impl.clientcomponents

import io.appmetrica.analytics.coreapi.internal.clientcomponents.ClientComponentsInitializer
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class DefaultClientComponentsInitializer : ClientComponentsInitializer {

    private val tag = "[DefaultClientComponentsInitializer]"

    private val moduleEntryPoints = listOf(
        "io.appmetrica.analytics.adrevenue.admob.v23.internal.AdMobClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.applovin.v12.internal.AppLovinClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.fyber.v3.internal.FyberClientModuleEntryPoint",
        "io.appmetrica.analytics.adrevenue.ironsource.v7.internal.IronSourceClientModuleEntryPoint",
        "io.appmetrica.analytics.apphud.internal.ApphudClientModuleEntryPoint",
        "io.appmetrica.analytics.screenshot.internal.ScreenshotClientModuleEntryPoint",
        "io.appmetrica.analytics.reporterextension.internal.ReporterExtensionClientModuleEntryPoint"
    )

    override fun onCreate() {
        if (ClientServiceLocator.getInstance().currentProcessDetector.isMainProcess) {
            DebugLogger.info(tag, "Register public modules")
            ClientServiceLocator.getInstance().moduleEntryPointsRegister.register(
                *moduleEntryPoints.map { ConstantModuleEntryPointProvider(it) }.toTypedArray()
            )
        } else {
            DebugLogger.info(tag, "Public modules not registered for non main process")
        }
    }
}
