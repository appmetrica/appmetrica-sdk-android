package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

private const val TAG = "[ModuleLoader]"

class ModuleLoader {

    fun loadModule(entryPointClassName: String): ModuleServiceEntryPoint<Any>? {
        YLogger.info(TAG, "Load module: $entryPointClassName")
        return ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<ModuleServiceEntryPoint<Any>>(
            entryPointClassName
        )
    }
}
