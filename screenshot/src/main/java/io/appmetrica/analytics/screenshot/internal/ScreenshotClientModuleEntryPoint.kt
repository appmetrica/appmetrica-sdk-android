package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ScreenshotCaptorsController
import io.appmetrica.analytics.screenshot.impl.config.client.BundleToClientSideScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

class ScreenshotClientModuleEntryPoint : ModuleClientEntryPoint<ClientSideScreenshotConfigWrapper>() {

    private val tag = "[ScreenshotClientModuleEntryPoint]"

    private var clientSideScreenshotConfig: ClientSideScreenshotConfig? = null

    private val bundleConverter = BundleToClientSideScreenshotConfigConverter()
    private val configUpdateListener = object : ServiceConfigUpdateListener<ClientSideScreenshotConfigWrapper> {
        override fun onServiceConfigUpdated(config: ModuleServiceConfig<ClientSideScreenshotConfigWrapper?>) {
            DebugLogger.info(tag, "Called onServiceConfigUpdated ${config.featuresConfig}")
            synchronized(this@ScreenshotClientModuleEntryPoint) {
                val newClientSideScreenshotConfig = config.featuresConfig?.config
                DebugLogger.info(tag, "New clientSideScreenshotConfig $newClientSideScreenshotConfig")
                clientSideScreenshotConfig = newClientSideScreenshotConfig
                if (this@ScreenshotClientModuleEntryPoint::screenshotCaptorsController.isInitialized) {
                    screenshotCaptorsController.updateConfig(clientSideScreenshotConfig)
                }
            }
        }
    }

    private lateinit var screenshotCaptorsController: ScreenshotCaptorsController

    override val identifier = Constants.MODULE_ID

    override val serviceConfigExtensionConfiguration =
        object : ServiceConfigExtensionConfiguration<ClientSideScreenshotConfigWrapper>() {

            override fun getBundleConverter() = bundleConverter

            override fun getServiceConfigUpdateListener() = configUpdateListener
        }

    override fun initClientSide(clientContext: ClientContext) {
        DebugLogger.info(tag, "Init Screenshot")
        synchronized(this@ScreenshotClientModuleEntryPoint) {
            screenshotCaptorsController = ScreenshotCaptorsController(clientContext)
        }
    }

    override fun onActivated() {
        DebugLogger.info(tag, "Called onActivated")
        synchronized(this@ScreenshotClientModuleEntryPoint) {
            if (this::screenshotCaptorsController.isInitialized) {
                screenshotCaptorsController.startCapture(clientSideScreenshotConfig)
            }
        }
    }
}
