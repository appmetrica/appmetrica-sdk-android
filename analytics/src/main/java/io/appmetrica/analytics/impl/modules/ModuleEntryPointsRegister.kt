package io.appmetrica.analytics.impl.modules

internal class ModuleEntryPointsRegister {

    private val _moduleEntryPoints = LinkedHashSet<ModuleEntryPointProvider>()

    val classNames: Set<String>
        @Synchronized
        get() = _moduleEntryPoints.map { it.className }.filter { it.isNotEmpty() }.toSet()

    @Synchronized
    fun register(vararg moduleEntryPointProvider: ModuleEntryPointProvider) {
        _moduleEntryPoints.addAll(moduleEntryPointProvider)
    }
}
