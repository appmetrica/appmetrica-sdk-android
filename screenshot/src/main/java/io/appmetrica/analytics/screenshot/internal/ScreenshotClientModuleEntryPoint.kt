package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener
import io.appmetrica.analytics.screenshot.impl.BundleToClientScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ScreenshotCaptorsController
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideRemoteScreenshotConfig
import io.appmetrica.analytics.screenshot.internal.config.ParcelableRemoteScreenshotConfig

class ScreenshotClientModuleEntryPoint : ModuleClientEntryPoint<ParcelableRemoteScreenshotConfig>() {

    private val tag = "[ScreenshotClientModuleEntryPoint]"

    private var clientSideRemoteScreenshotConfig: ClientSideRemoteScreenshotConfig? = null

    private val bundleConverter = BundleToClientScreenshotConfigConverter()
    private val configUpdateListener = object : ServiceConfigUpdateListener<ParcelableRemoteScreenshotConfig> {
        override fun onServiceConfigUpdated(config: ModuleServiceConfig<ParcelableRemoteScreenshotConfig?>) {
            DebugLogger.info(tag, "Called onServiceConfigUpdated ${config.featuresConfig}")
            synchronized(this@ScreenshotClientModuleEntryPoint) {
                val newClientSideRemoteScreenshotConfig = config.featuresConfig?.let {
                    ClientSideRemoteScreenshotConfig(it)
                }
                DebugLogger.info(tag, "New clientRemoteScreenshotConfig $newClientSideRemoteScreenshotConfig")
                clientSideRemoteScreenshotConfig = newClientSideRemoteScreenshotConfig
                if (this@ScreenshotClientModuleEntryPoint::screenshotCaptorsController.isInitialized) {
                    screenshotCaptorsController.updateConfig(clientSideRemoteScreenshotConfig)
                }
            }
        }
    }

    private lateinit var screenshotCaptorsController: ScreenshotCaptorsController

    override val identifier = Constants.MODULE_ID

    override val serviceConfigExtensionConfiguration =
        object : ServiceConfigExtensionConfiguration<ParcelableRemoteScreenshotConfig>() {

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
                screenshotCaptorsController.startCapture(clientSideRemoteScreenshotConfig)
            }
        }
    }
}
