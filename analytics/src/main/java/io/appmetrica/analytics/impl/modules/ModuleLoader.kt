package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint

private const val TAG = "[ModuleLoader]"

class ModuleLoader {

    fun loadModule(entryPointClassName: String): ModuleEntryPoint<Any>? {
        YLogger.info(TAG, "Load module: $entryPointClassName")
        return ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<ModuleEntryPoint<Any>>(entryPointClassName)
    }
}
