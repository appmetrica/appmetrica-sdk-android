package io.appmetrica.analytics.modulesapi.internal.service

import android.os.Bundle

/**
 * This interface is needed to transfer configuration from the service side of the module to the client side.
 * It is used every time the core is about to send some configuration from the service to the client.
 */
interface ClientConfigProvider {

    /**
     * Returns Bundle with configuration that is needed to be transferred to client side of the module.
     * Returns null if no transfer needed.
     */
    fun getConfigBundleForClient(): Bundle?
}
