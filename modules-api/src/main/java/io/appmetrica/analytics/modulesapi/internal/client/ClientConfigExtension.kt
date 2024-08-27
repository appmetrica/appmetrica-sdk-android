package io.appmetrica.analytics.modulesapi.internal.client

interface ClientConfigExtension {

    val clientConfigListener: ClientConfigListener

    fun doesModuleNeedConfig(): Boolean
}
