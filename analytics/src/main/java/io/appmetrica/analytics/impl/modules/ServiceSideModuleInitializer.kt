package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.ServiceContext

internal interface ServiceSideModuleInitializer {

    fun initServiceSide(serviceContext: ServiceContext, startupState: StartupState)
}
