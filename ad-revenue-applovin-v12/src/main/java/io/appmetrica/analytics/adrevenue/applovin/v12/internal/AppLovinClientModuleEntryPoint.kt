package io.appmetrica.analytics.adrevenue.applovin.v12.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.impl.AdRevenueConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.impl.AppLovinAdRevenueProcessor
import io.appmetrica.analytics.adrevenue.applovin.v12.impl.Constants.LIBRARY_MAIN_CLASS
import io.appmetrica.analytics.adrevenue.applovin.v12.impl.Constants.MODULE_ID
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint

class AppLovinClientModuleEntryPoint : ModuleClientEntryPoint<Any> {

    private val tag = "[AppLovinClientModuleEntryPoint]"
    override val identifier: String = MODULE_ID

    override fun initClientSide(clientContext: ClientContext) {
        DebugLogger.info(tag, "initClientSide")
        if (ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)) {
            clientContext.moduleAdRevenueContext.adRevenueProcessorsHolder.register(
                AppLovinAdRevenueProcessor(
                    AdRevenueConverter(),
                    clientContext
                )
            )
        } else {
            DebugLogger.info(tag, "$LIBRARY_MAIN_CLASS not found")
        }
    }

    override fun onActivated() {
        DebugLogger.info(tag, "onActivated")
    }
}
