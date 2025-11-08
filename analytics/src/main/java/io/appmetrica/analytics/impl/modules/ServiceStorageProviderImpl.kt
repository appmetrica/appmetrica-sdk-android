package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreapi.internal.data.ProtobufBinaryStateStorageFactory
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.service.ServiceStorageProvider
import io.appmetrica.analytics.protobuf.nano.MessageNano
import java.io.File

internal class ServiceStorageProviderImpl(
    private val context: Context,
    private val preferencesDbStorage: SimplePreferenceStorage,
    override val dbStorage: SQLiteOpenHelper
) : ServiceStorageProvider {

    override fun modulePreferences(moduleIdentifier: String): ModulePreferences =
        ModulePreferencesAdapter(moduleIdentifier, preferencesDbStorage)

    override fun legacyModulePreferences(): ModulePreferences =
        LegacyModulePreferenceAdapter(preferencesDbStorage)

    override val tempCacheStorage: TempCacheStorage
        get() = GlobalServiceLocator.getInstance().storageFactory.getServiceTempCacheStorage(context)

    override val appFileStorage: File?
        get() = FileUtils.getAppStorageDirectory(context)

    override val appDataStorage: File?
        get() = FileUtils.getAppDataDir(context)

    override val sdkDataStorage: File?
        get() = FileUtils.sdkStorage(context)

    override fun <T, P : MessageNano> createBinaryStateStorageFactory(
        key: String,
        serializer: ProtobufStateSerializer<P>,
        converter: ProtobufConverter<T, P>
    ): ProtobufBinaryStateStorageFactory<T> = StorageFactory.Provider.createCustomFactory(
        key,
        serializer,
        converter
    )
}
