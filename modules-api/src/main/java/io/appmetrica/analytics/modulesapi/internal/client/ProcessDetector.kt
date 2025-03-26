package io.appmetrica.analytics.modulesapi.internal.client

interface ProcessDetector {

    fun isMainProcess(): Boolean
}
