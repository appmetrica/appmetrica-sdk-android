package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.AppLovinIlrdAdapter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.AppLovinIlrdReporter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.BundleToClientApplovinConfigConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueCollector

class AppLovinAutoClientModuleEntryPoint : ModuleClientEntryPoint<ClientApplovinConfigWrapper>() {

    private val tag = "[AppLovinAutoClientModuleEntryPoint]"

    override val identifier = Constants.MODULE_ID

    private var clientConfig: ClientApplovinConfig? = null
    private var libraryAvailable = false

    private var classExists: Boolean? = null

    private var adapter: AppLovinIlrdAdapter? = null

    private val bundleConverter = BundleToClientApplovinConfigConverter()
    private val configUpdateListener =
        object : ServiceConfigUpdateListener<ClientApplovinConfigWrapper> {
            override fun onServiceConfigUpdated(
                config: ModuleServiceConfig<ClientApplovinConfigWrapper?>
            ) {
                DebugLogger.info(tag, "Called onServiceConfigUpdated ${config.featuresConfig}")
                synchronized(this@AppLovinAutoClientModuleEntryPoint) {
                    clientConfig = config.featuresConfig?.config
                }
                updateListenerState()
            }
        }

    override val serviceConfigExtensionConfiguration =
        object : ServiceConfigExtensionConfiguration<ClientApplovinConfigWrapper>() {
            override fun getBundleConverter() = bundleConverter
            override fun getServiceConfigUpdateListener() = configUpdateListener
        }

    override fun initClientSide(clientContext: ClientContext) {
        DebugLogger.info(tag, "initClientSide")
        synchronized(this) {
            adapter = AppLovinIlrdAdapter(clientContext.context, AppLovinIlrdReporter(clientContext))
        }
    }

    override fun onActivated() {
        DebugLogger.info(tag, "onActivated")
        updateListenerState()
    }

    private fun updateListenerState() {
        synchronized(this) {
            val currentAdapter = adapter ?: return
            val enabled = clientConfig?.enabled ?: Constants.Defaults.DEFAULT_ENABLED
            val classesExist = isClassExists()
            val shouldBeAvailable = enabled && classesExist
            DebugLogger.info(
                tag,
                "updateListenerState: enabled=$enabled, classExists=$classesExist, shouldBeAvailable=$shouldBeAvailable"
            )
            if (shouldBeAvailable == libraryAvailable) return
            if (shouldBeAvailable) {
                if (currentAdapter.registerSubscriber()) {
                    libraryAvailable = true
                }
            } else {
                if (currentAdapter.unregisterSubscriber()) {
                    libraryAvailable = false
                }
            }
            DebugLogger.info(
                tag,
                "updateListenerState result: shouldBeAvailable=$shouldBeAvailable, libraryAvailable=$libraryAvailable"
            )
        }
    }

    override val adRevenueCollector: AdRevenueCollector = object : AdRevenueCollector {
        override val sourceIdentifier: String
            get() = Constants.AD_REVENUE_SOURCE_IDENTIFIER

        override val enabled: Boolean
            get() = synchronized(this@AppLovinAutoClientModuleEntryPoint) {
                DebugLogger.info(
                    tag,
                    "adRevenueCollector.enabled: config=${clientConfig?.enabled} library=$libraryAvailable"
                )
                (clientConfig?.enabled ?: Constants.Defaults.DEFAULT_ENABLED) && libraryAvailable
            }
    }

    private fun isClassExists(): Boolean {
        return classExists ?: (
            ReflectionUtils.detectClassExists(Constants.LIBRARY_COMMUNICATOR_CLASS) &&
                ReflectionUtils.detectClassExists(Constants.LIBRARY_MESSAGE_CLASS)
            ).also { classExists = it }
    }
}
