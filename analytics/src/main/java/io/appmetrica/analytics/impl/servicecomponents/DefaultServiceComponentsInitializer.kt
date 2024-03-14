package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.impl.modules.PreferencesBasedModuleEntryPoint

class DefaultServiceComponentsInitializer : ServiceComponentsInitializer {

    override fun onCreate(context: Context) {
        GlobalServiceLocator.getInstance().moduleEntryPointsRegister.register(
            ConstantModuleEntryPointProvider(
                "io.appmetrica.analytics.remotepermissions.internal.RemotePermissionsModuleEntryPoint"
            ),
            PreferencesBasedModuleEntryPoint(
                context,
                "io.appmetrica.analytics.modules.ads",
                "lsm"
            )
        )
    }
}
