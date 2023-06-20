package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.startup.StartupState

class UuidFromStartupStateImporter : IOuterSourceUuidImporter {

    override fun get(context: Context): String? =
        StorageFactory.Provider.get(StartupState::class.java).create(context).read().uuid
}
