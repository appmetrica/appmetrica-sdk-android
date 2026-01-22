package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.sdk.AppLovinSdk
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor

internal class AppLovinAdRevenueProcessor(
    private val converter: AdRevenueConverter,
    private val clientContext: ClientContext
) : ModuleAdRevenueProcessor {

    override fun process(vararg values: Any?): Boolean {
        val isArgumentsHasClasses = ReflectionUtils.isArgumentsOfClasses(
            values,
            MaxAd::class.java,
            AppLovinSdk::class.java
        )
        if (!isArgumentsHasClasses) return false
        val maxAd = values.getOrNull(0) as MaxAd
        val appLovinSdk = values.getOrNull(1) as AppLovinSdk

        val adRevenue = converter.convert(maxAd, appLovinSdk)
        clientContext.internalClientModuleFacade.reportAdRevenue(adRevenue)
        LoggerStorage.getMainPublicOrAnonymousLogger().info(
            "Ad Revenue from AppLovin with values ${values.contentToString()} was reported"
        )
        return true
    }

    override fun getDescription() = "AppLovin"
}
