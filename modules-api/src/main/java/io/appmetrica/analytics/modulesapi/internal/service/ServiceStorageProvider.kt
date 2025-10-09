package io.appmetrica.analytics.modulesapi.internal.service

import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreapi.internal.data.ProtobufBinaryStateStorageFactory
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.protobuf.nano.MessageNano
import java.io.File

interface ServiceStorageProvider {

    fun modulePreferences(moduleIdentifier: String): ModulePreferences

    fun legacyModulePreferences(): ModulePreferences

    val dbStorage: SQLiteOpenHelper

    val tempCacheStorage: TempCacheStorage

    val appFileStorage: File?

    val appDataStorage: File?

    val sdkDataStorage: File?

    fun <T, P : MessageNano> createBinaryStateStorageFactory(
        key: String,
        serializer: ProtobufStateSerializer<P>,
        converter: ProtobufConverter<T, P>
    ): ProtobufBinaryStateStorageFactory<T>
}
