package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.db.storage.ServiceStorageFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class ServiceStorageProviderImplTest : CommonTest() {

    private val context: Context = mock()
    private val preferenceServiceDbStorage = mock<PreferencesServiceDbStorage>()
    private val dbStorage = mock<SQLiteOpenHelper>()
    private val identifier = "moduleIdentifier"

    @get:Rule
    val modulePreferencesAdapterMockedRule = MockedConstructionRule(ModulePreferencesAdapter::class.java)

    @get:Rule
    val legacyModulePreferencesAdapterMockedRule = MockedConstructionRule(LegacyModulePreferenceAdapter::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val tempCacheStorage: TempCacheStorage = mock()

    private val databaseStorageFactory: ServiceStorageFactory = mock {
        on { getServiceTempCacheStorage(context) } doReturn tempCacheStorage
    }

    private val appFileStorage: File = mock()
    private val appDataStorage: File = mock()
    private val sdkDataStorage: File = mock()

    @get:Rule
    val fileUtilsMockedStaticRule = staticRule<FileUtils> {
        on { FileUtils.getAppStorageDirectory(context) } doReturn appFileStorage
        on { FileUtils.getAppDataDir(context) } doReturn appDataStorage
        on { FileUtils.sdkStorage(context) } doReturn sdkDataStorage
    }

    private val storageProviderImpl by setUp {
        whenever(GlobalServiceLocator.getInstance().getStorageFactory()).thenReturn(databaseStorageFactory)
        ServiceStorageProviderImpl(context, preferenceServiceDbStorage, dbStorage)
    }

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

    @Test
    fun tempCacheStorage() {
        assertThat(storageProviderImpl.tempCacheStorage).isEqualTo(tempCacheStorage)
    }

    @Test
    fun appFileStorage() {
        assertThat(storageProviderImpl.appFileStorage).isEqualTo(appFileStorage)
    }

    @Test
    fun appDataStorage() {
        assertThat(storageProviderImpl.appDataStorage).isEqualTo(appDataStorage)
    }

    @Test
    fun sdkDataStorage() {
        assertThat(storageProviderImpl.sdkDataStorage).isEqualTo(sdkDataStorage)
    }
}
