package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage

internal class VitalDataProviderStorage(private val context: Context) {

    private val componentProviders = mutableMapOf<String, VitalComponentDataProvider>()

    private val vitalDataSource: FileVitalDataSource = FileVitalDataSource(
        context,
        VitalCommonDataProvider.BACKUP_FILE_NAME,
        GlobalServiceLocator.getInstance().serviceExecutorProvider.persistenceExecutor
    )

    val commonDataProvider: VitalCommonDataProvider = VitalCommonDataProvider(
        GlobalServiceLocator.getInstance().servicePreferences,
        vitalDataSource
    )

    val commonDataProviderForMigration: VitalCommonDataProvider = VitalCommonDataProvider(
        PreferencesServiceDbStorage(
            GlobalServiceLocator.getInstance().storageFactory.getServicePreferenceDbHelperForMigration(context)
        ),
        vitalDataSource
    )

    @Synchronized
    fun getComponentDataProvider(componentId: ComponentId): VitalComponentDataProvider {
        val key = "$componentId"
        return componentProviders.getOrPut(key) {
            VitalComponentDataProvider(
                PreferencesComponentDbStorage(
                    GlobalServiceLocator.getInstance().storageFactory.getComponentPreferenceDbHelper(
                        context,
                        componentId
                    ),
                ),
                getComponentBackupVitalDataSource(componentId),
                key
            )
        }
    }

    private fun getComponentBackupVitalDataSource(componentId: ComponentId): VitalDataSource {
        val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.persistenceExecutor
        return if (componentId.isMain) {
            val actual = "appmetrica_vital_main.dat"
            val legacy = composeFileNameForNonMain(componentId)
            CompositeFileVitalDataSource(
                listOf(
                    legacy to FileVitalDataSource(context, legacy, executor),
                    actual to FileVitalDataSource(context, actual, executor)
                )
            )
        } else {
            FileVitalDataSource(context, composeFileNameForNonMain(componentId), executor)
        }
    }

    private fun composeFileNameForNonMain(componentId: ComponentId): String =
        "appmetrica_vital_${componentId.apiKey}.dat"
}
