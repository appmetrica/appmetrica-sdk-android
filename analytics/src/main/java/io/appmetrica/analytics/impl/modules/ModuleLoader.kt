package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.internal.YLogger

class ModuleLoader {

    inline fun <reified T> loadModule(entryPointClassName: String): T? {
        YLogger.info("[ModuleLoader]", "Load module: $entryPointClassName")
        return ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<T>(
            entryPointClassName
        )
    }
}
