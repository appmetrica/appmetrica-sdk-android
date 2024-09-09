package io.appmetrica.analytics.modulesapi.internal.client

abstract class ServiceConfigExtensionConfiguration<T : Any> {

    abstract fun getBundleConverter(): BundleToServiceConfigConverter<T>

    abstract fun getServiceConfigUpdateListener(): ServiceConfigUpdateListener<T>
}
