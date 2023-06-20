package io.appmetrica.analytics.impl.clientcomponents

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.clientcomponents.ClientComponentsInitializer
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils

internal class ClientComponentsInitializerProvider {

    fun getClientComponentsInitializer(): ClientComponentsInitializer {
        if (BuildConfig.CLIENT_COMPONENTS_INITIALIZER_CLASS_NAME.isNotEmpty()) {
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<ClientComponentsInitializer>(
                BuildConfig.CLIENT_COMPONENTS_INITIALIZER_CLASS_NAME
            )?.let {
                return it
            }
        }
        return DefaultClientComponentsInitializer()
    }
}
