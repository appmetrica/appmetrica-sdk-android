package io.appmetrica.analytics.impl.modules.client

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration

internal class ClientModuleServiceConfigModelFactory {

    fun <T : Any> createClientModuleServiceConfigModel(
        bundle: Bundle,
        moduleIdentifier: String,
        identifiers: SdkIdentifiers,
        extensionConfiguration: ServiceConfigExtensionConfiguration<T>
    ): ClientModuleServiceConfigModel<T?>? {
        return bundle.getBundle(moduleIdentifier)?.let { moduleConfig ->
            ClientModuleServiceConfigModel(
                identifiers = identifiers,
                featuresConfig = extensionConfiguration.getBundleConverter().fromBundle(moduleConfig)
            )
        }
    }
}
