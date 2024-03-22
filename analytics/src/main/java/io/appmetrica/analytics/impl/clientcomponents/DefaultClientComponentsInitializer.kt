package io.appmetrica.analytics.impl.clientcomponents

import io.appmetrica.analytics.coreapi.internal.clientcomponents.ClientComponentsInitializer
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.modules.ConstantModuleEntryPointProvider
import io.appmetrica.analytics.logger.internal.YLogger

class DefaultClientComponentsInitializer : ClientComponentsInitializer {

    private val tag = "[PublicClientComponentsInitializer]"

    private val moduleEntryPoints = listOf<String>()

    override fun onCreate() {
        YLogger.info(tag, "Register public modules")
        ClientServiceLocator.getInstance().moduleEntryPointsRegister.register(
            *moduleEntryPoints.map { ConstantModuleEntryPointProvider(it) }.toTypedArray()
        )
    }
}
