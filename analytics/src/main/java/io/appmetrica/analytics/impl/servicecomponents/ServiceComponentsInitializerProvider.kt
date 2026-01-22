package io.appmetrica.analytics.impl.servicecomponents

import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils

internal class ServiceComponentsInitializerProvider {

    fun getServiceComponentsInitializer(): ServiceComponentsInitializer {
        if (BuildConfig.SERVICE_COMPONENTS_INITIALIZER_CLASS_NAME.isNotEmpty()) {
            ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<ServiceComponentsInitializer>(
                BuildConfig.SERVICE_COMPONENTS_INITIALIZER_CLASS_NAME
            )?.let {
                return it
            }
        }
        return DefaultServiceComponentsInitializer()
    }
}
