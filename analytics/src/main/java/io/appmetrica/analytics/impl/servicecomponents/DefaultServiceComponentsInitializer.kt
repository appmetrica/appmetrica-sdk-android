package io.appmetrica.analytics.impl.servicecomponents

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.impl.GlobalServiceLocator

class DefaultServiceComponentsInitializer : ServiceComponentsInitializer {

    override fun onCreate(context: Context) {
        GlobalServiceLocator.getInstance().moduleEntryPointsRegister.register(
            "io.appmetrica.analytics.remotepermissions.internal.RemotePermissionsModuleEntryPoint",
        )
    }
}
