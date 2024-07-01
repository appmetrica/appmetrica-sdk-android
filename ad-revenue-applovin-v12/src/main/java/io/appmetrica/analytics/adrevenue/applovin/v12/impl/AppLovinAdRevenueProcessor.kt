package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.sdk.AppLovinSdk
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor

class AppLovinAdRevenueProcessor(
    private val converter: AdRevenueConverter,
    private val clientContext: ClientContext
) : ModuleAdRevenueProcessor {

    override fun process(vararg values: Any): Boolean {
        if (values.size != 2) return false
        val maxAd = values.getOrNull(0) as? MaxAd ?: return false
        val appLovinSdk = values.getOrNull(1) as? AppLovinSdk ?: return false

        val adRevenue = converter.convert(maxAd, appLovinSdk)
        clientContext.moduleAdRevenueContext.adRevenueReporter.reportAutoAdRevenue(adRevenue)
        LoggerStorage.getMainPublicOrAnonymousLogger().info(
            "Ad Revenue from AppLovin with values ${values.contentToString()} was reported"
        )
        return true
    }

    override fun getDescription() = "AppLovin"
}
