package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory

internal class VitalDataProviderStorage(private val context: Context) {

    private val componentProviders = mutableMapOf<String, VitalComponentDataProvider>()

    private val vitalDataSource: FileVitalDataSource = FileVitalDataSource(
        context,
        VitalCommonDataProvider.BACKUP_FILE_NAME
    )

    val commonDataProvider: VitalCommonDataProvider = VitalCommonDataProvider(
        GlobalServiceLocator.getInstance().servicePreferences,
        vitalDataSource
    )

    val commonDataProviderForMigration: VitalCommonDataProvider = VitalCommonDataProvider(
        PreferencesServiceDbStorage(
            DatabaseStorageFactory.getInstance(context).preferencesDbHelperForServiceMigration
        ),
        vitalDataSource
    )

    @Synchronized
    fun getComponentDataProvider(componentId: ComponentId): VitalComponentDataProvider {
        val key = "${componentId.apiKey}"
        return componentProviders.getOrPut(key) {
            VitalComponentDataProvider(
                PreferencesComponentDbStorage(
                    DatabaseStorageFactory.getInstance(context).getPreferencesDbHelper(componentId),
                ),
                FileVitalDataSource(
                    context,
                    VitalComponentDataProvider.composeFileName(componentId.apiKey)
                ),
                key
            )
        }
    }
}
