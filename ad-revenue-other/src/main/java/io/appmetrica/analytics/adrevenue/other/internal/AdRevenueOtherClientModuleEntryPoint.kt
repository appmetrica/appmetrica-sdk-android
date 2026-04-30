package io.appmetrica.analytics.adrevenue.other.internal

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.client.BundleToClientSideAdRevenueOtherConfigConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.impl.fb.FBAdRevenueAdapter
import io.appmetrica.analytics.adrevenue.other.impl.fb.FBConstants
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueCollector
import java.util.concurrent.atomic.AtomicBoolean

class AdRevenueOtherClientModuleEntryPoint :
    ModuleClientEntryPoint<ClientSideAdRevenueOtherConfigWrapper>() {

    private val tag = "[AdRevenueOtherClientModuleEntryPoint]"

    private var clientContext: ClientContext? = null
    private var clientConfig: ClientSideAdRevenueOtherConfig? = null
    private val libraryAvailable = AtomicBoolean(false)

    private val fbAdapter = FBAdRevenueAdapter()
    private val bundleConverter = BundleToClientSideAdRevenueOtherConfigConverter()
    private val configUpdateListener =
        object : ServiceConfigUpdateListener<ClientSideAdRevenueOtherConfigWrapper> {
            override fun onServiceConfigUpdated(
                config: ModuleServiceConfig<ClientSideAdRevenueOtherConfigWrapper?>
            ) {
                DebugLogger.info(tag, "Called onServiceConfigUpdated ${config.featuresConfig}")
                synchronized(this@AdRevenueOtherClientModuleEntryPoint) {
                    clientConfig = config.featuresConfig?.config
                }
                updateListenerState()
            }
        }

    override val identifier = Constants.MODULE_ID

    override val serviceConfigExtensionConfiguration =
        object : ServiceConfigExtensionConfiguration<ClientSideAdRevenueOtherConfigWrapper>() {
            override fun getBundleConverter() = bundleConverter
            override fun getServiceConfigUpdateListener() = configUpdateListener
        }

    override fun initClientSide(clientContext: ClientContext) {
        DebugLogger.info(tag, "initClientSide")
        this.clientContext = clientContext
    }

    override fun onActivated() {
        DebugLogger.info(tag, "onActivated")
        updateListenerState()
    }

    private fun updateListenerState() {
        synchronized(this) {
            val ctx = clientContext ?: return
            val enabled = clientConfig?.enabled == true
            val classExists = ReflectionUtils.detectClassExists(FBConstants.LIBRARY_MAIN_CLASS)
            if (enabled && classExists) {
                fbAdapter.registerListener(ctx)
                libraryAvailable.set(true)
                DebugLogger.info(tag, "Facebook listener registered")
            } else {
                fbAdapter.unregisterListener()
                libraryAvailable.set(false)
                DebugLogger.info(
                    tag,
                    "Facebook listener not registered: " +
                        "enabled=$enabled, class=${FBConstants.LIBRARY_MAIN_CLASS}"
                )
            }
        }
    }

    override val adRevenueCollector: AdRevenueCollector = object : AdRevenueCollector {
        override val sourceIdentifier: String
            get() = FBConstants.AD_REVENUE_SOURCE_IDENTIFIER

        override val enabled: Boolean
            get() = synchronized(this@AdRevenueOtherClientModuleEntryPoint) {
                DebugLogger.info(tag, "enabled: ${clientConfig?.includeSource} ${libraryAvailable.get()}")
                clientConfig?.includeSource == true && libraryAvailable.get()
            }
    }
}
