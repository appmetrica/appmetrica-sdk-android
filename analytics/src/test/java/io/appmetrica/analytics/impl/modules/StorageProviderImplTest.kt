package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class StorageProviderImplTest : CommonTest() {

    private val context: Context = mock()
    private val preferenceServiceDbStorage = mock<PreferencesServiceDbStorage>()
    private val dbStorage = mock<SQLiteOpenHelper>()
    private val identifier = "moduleIdentifier"

    @get:Rule
    val modulePreferencesAdapterMockedRule = MockedConstructionRule(ModulePreferencesAdapter::class.java)

    @get:Rule
    val legacyModulePreferencesAdapterMockedRule = MockedConstructionRule(LegacyModulePreferenceAdapter::class.java)

    private val storageProviderImpl = ServiceStorageProviderImpl(context, preferenceServiceDbStorage, dbStorage)

    @Test
    fun modulePreferences() {
        assertThat(storageProviderImpl.modulePreferences(identifier))
            .isEqualTo(modulePreferencesAdapterMockedRule.constructionMock.constructed()[0])
        assertThat(modulePreferencesAdapterMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(modulePreferencesAdapterMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(identifier, preferenceServiceDbStorage)
    }

    @Test
    fun legacyModulePreferences() {
        assertThat(storageProviderImpl.legacyModulePreferences())
            .isEqualTo(legacyModulePreferencesAdapterMockedRule.constructionMock.constructed()[0])
        assertThat(legacyModulePreferencesAdapterMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(legacyModulePreferencesAdapterMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(preferenceServiceDbStorage)
    }
}
