package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig

internal class ClientModuleServiceConfigModel<T>(
    override val identifiers: SdkIdentifiers,
    override val featuresConfig: T
) : ModuleServiceConfig<T>
