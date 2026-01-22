package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.utils.DebugAssert

internal class TempCacheDbHelperWrapper(
    private val context: Context,
    private val storageType: StorageType,
    private val storage: TempCacheStorage
) : TempCacheStorage {

    override fun put(scope: String, timestamp: Long, data: ByteArray): Long {
        checkMigrated()
        return storage.put(scope, timestamp, data)
    }

    override fun get(scope: String): TempCacheStorage.Entry? {
        checkMigrated()
        return storage.get(scope)
    }

    override fun get(scope: String, limit: Int): Collection<TempCacheStorage.Entry> {
        checkMigrated()
        return storage.get(scope, limit)
    }

    override fun remove(id: Long) {
        checkMigrated()
        storage.remove(id)
    }

    override fun removeOlderThan(scope: String, interval: Long) {
        checkMigrated()
        storage.removeOlderThan(scope, interval)
    }

    private fun checkMigrated() {
        if (BuildConfig.METRICA_DEBUG) {
            DebugAssert.assertMigrated(context, storageType)
        }
    }
}
