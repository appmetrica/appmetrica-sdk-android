package io.appmetrica.analytics.modulesapi.internal.client

import android.os.Bundle

/**
 * This interface is needed to transfer configuration from the service side of the module to the client side.
 * It is used every time the core is about to send some configuration from the service to the client.
 */
interface ClientConfigListener {

    /**
     * Receives a Bundle with configuration for the client side of the module.
     */
    fun onConfigReceived(data: Bundle)
}
