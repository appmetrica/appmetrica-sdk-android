package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.logger.internal.YLogger

class UuidFromStartupStateImporter : IOuterSourceUuidImporter {

    private val tag = "[UuidFromStartupStateImporter]"

    override fun get(context: Context): String? = try {
        StorageFactory.Provider.get(StartupStateModel::class.java)?.create(context)?.read()?.uuid
    } catch (e: Throwable) {
        YLogger.error(tag, e)
        null
    }
}
