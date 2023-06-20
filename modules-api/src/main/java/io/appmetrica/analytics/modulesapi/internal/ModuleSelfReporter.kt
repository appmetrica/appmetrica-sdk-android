package io.appmetrica.analytics.modulesapi.internal

interface ModuleSelfReporter {

    fun reportEvent(eventName: String)

    fun reportEvent(eventName: String, eventValue: Map<String, Any>?)

    fun reportEvent(eventName: String, eventValue: String?)

    fun reportEvent(type: Int, eventName: String, eventValue: String?)

    fun reportError(message: String, error: Throwable? = null)

    fun reportError(identifier: String, message: String? = null)
}
