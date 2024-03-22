package io.appmetrica.analytics.impl.modules.service

import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal interface ServiceSideModuleInitializer {

    fun initServiceSide(serviceContext: ServiceContext, startupState: StartupState)
}
