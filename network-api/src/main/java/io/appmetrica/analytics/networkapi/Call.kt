package io.appmetrica.analytics.networkapi

/**
 * Represents a network call that can be executed to get a [Response].
 * This is an abstract class that must be implemented by concrete network clients.
 */
abstract class Call {

    /**
     * Executes the network call synchronously and returns the response.
     *
     * @return [Response] object containing the result of the network call
     */
    abstract fun execute(): Response
}
