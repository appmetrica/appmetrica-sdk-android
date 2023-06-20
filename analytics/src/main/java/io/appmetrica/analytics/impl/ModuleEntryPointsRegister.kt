package io.appmetrica.analytics.impl

internal class ModuleEntryPointsRegister {

    private val _moduleEntryPoints = LinkedHashSet<String>()

    val classNames: Set<String>
        @Synchronized
        get() = LinkedHashSet(_moduleEntryPoints)

    @Synchronized
    fun register(vararg classNames: String) {
        _moduleEntryPoints.addAll(classNames)
    }
}
