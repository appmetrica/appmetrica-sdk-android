package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.impl.modules.PreferencesBasedModuleEntryPoint

internal class DefaultServiceComponentsInitializer : ServiceComponentsInitializer {

    // order may be important
    private val moduleEntryPoints = listOf(
        "io.appmetrica.analytics.remotepermissions.internal.RemotePermissionsModuleEntryPoint",
        "io.appmetrica.analytics.apphud.internal.ApphudServiceModuleEntryPoint",
        "io.appmetrica.analytics.screenshot.internal.ScreenshotServiceModuleEntryPoint",
        "io.appmetrica.analytics.billing.internal.BillingServiceModuleEntryPoint",
        "io.appmetrica.analytics.idsync.internal.IdSyncModuleEntryPoint"
    )

    override fun onCreate(context: Context) {
        GlobalServiceLocator.getInstance().moduleEntryPointsRegister.register(
            *moduleEntryPoints.map { ConstantModuleEntryPointProvider(it) }.toTypedArray()
        )
        GlobalServiceLocator.getInstance().moduleEntryPointsRegister.register(
            PreferencesBasedModuleEntryPoint(
                context,
                "io.appmetrica.analytics.modules.ads",
                "lsm"
            )
        )
    }
}
