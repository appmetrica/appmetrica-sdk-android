package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import io.appmetrica.analytics.impl.MigrationManager.MigrationScript
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceMigrationScriptToV115 : MigrationScript {

    private val tag = "[ServiceMigrationScriptToV115]"

    override fun run(context: Context) {
        DebugLogger.info(tag, "Run migration")
        val storageFactory = StorageFactory.Provider.get(StartupStateModel::class.java).createForMigration(context)
        storageFactory.save(
            storageFactory
                .read()
                .buildUpon()
                .withObtainTime(0L)
                .build()
        )
    }
}
