package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.internal.DebugLogger

class ModuleLoader {

    inline fun <reified T> loadModule(entryPointClassName: String): T? {
        DebugLogger.info("[ModuleLoader]", "Load module: $entryPointClassName")
        return ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<T>(
            entryPointClassName
        )
    }
}
