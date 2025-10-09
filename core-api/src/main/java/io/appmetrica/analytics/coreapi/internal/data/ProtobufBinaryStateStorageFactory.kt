package io.appmetrica.analytics.coreapi.internal.data

import android.content.Context

interface ProtobufBinaryStateStorageFactory<T> {
    fun create(context: Context): ProtobufStateStorage<T>
    fun createForMigration(context: Context): ProtobufStateStorage<T>
}
