package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.network.internal.NetworkClientServiceLocator
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings

internal object NetworkClientFactory {

    private const val TAG = "[NetworkClientFactory]"

    @JvmStatic
    fun createNetworkClient(
        settings: NetworkClientSettings,
    ): NetworkClient {
        val builderClasses = listOfNotNull(
            getCustomNetworkClientBuilderClassName(),
            "io.appmetrica.analytics.networkokhttp.internal.OkHttpNetworkClientBuilder",
            "io.appmetrica.analytics.networklegacy.internal.LegacyNetworkClientBuilder",
        )

        val clientBuilder = builderClasses.firstNotNullOfOrNull { builderClass ->
            DebugLogger.info(TAG, "Trying to create $builderClass")
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
                builderClass,
                NetworkClient.Builder::class.java
            )
        } ?: DummyNetworkClientBuilder()

        PublicLogger.getAnonymousInstance().info("Created $clientBuilder")

        return clientBuilder
            .withSettings(settings)
            .build()
    }

    private fun getCustomNetworkClientBuilderClassName(): String? {
        return NetworkClientServiceLocator.getInstance()
            .applicationMetaData
            ?.getString(Constants.CUSTOM_NETWORK_CLIENT_BUILDER_PROPERTY)
    }
}
