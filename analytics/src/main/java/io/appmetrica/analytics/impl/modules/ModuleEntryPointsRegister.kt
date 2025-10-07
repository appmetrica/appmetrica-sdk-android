package io.appmetrica.analytics.impl.modules

internal class ModuleEntryPointsRegister {

    private val _moduleEntryPoints = mutableListOf<ModuleEntryPointProvider>()

    val classNames: List<String>
        @Synchronized
        get() = _moduleEntryPoints.map { it.className }.filter { it.isNotEmpty() }.distinct()

    @Synchronized
    fun register(vararg moduleEntryPointProvider: ModuleEntryPointProvider) {
        _moduleEntryPoints.addAll(moduleEntryPointProvider)
    }
}
