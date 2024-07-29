package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import io.appmetrica.analytics.testutils.TestUtils
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class VitalDataProviderStorageTest : CommonTest() {

    @get:Rule
    val vitalCommonDataProviderMockedRule = MockedConstructionRule(VitalCommonDataProvider::class.java)
    @get:Rule
    val globalServiceLocation = GlobalServiceLocatorRule()
    @get:Rule
    val fileVitalDataSourceMockedRule = MockedConstructionRule(FileVitalDataSource::class.java)
    @get:Rule
    val vitalComponentDataProviderMockedRule = MockedConstructionRule(VitalComponentDataProvider::class.java)
    @get:Rule
    val preferenceComponentDbStorageMockedRule = MockedConstructionRule(PreferencesComponentDbStorage::class.java)
    @get:Rule
    val databaseStorageFactoryMockedRule = MockedStaticRule(DatabaseStorageFactory::class.java)
    @get:Rule
    val prefereceServiceDbStorageMockedRule = MockedConstructionRule(PreferencesServiceDbStorage::class.java)
    @get:Rule
    val compositeFileVitalDataSourceMockedConstructionRule = constructionRule<CompositeFileVitalDataSource>()

    private val firstApiKey = "first"
    private val secondApiKey = "second"
    private val firstComponentId = mock<ComponentId> {
        on { apiKey } doReturn firstApiKey
        on { isMain } doReturn true
    }
    private val secondComponentId = mock<ComponentId> {
        on { apiKey } doReturn secondApiKey
        on { isMain } doReturn false
    }
    private val firstKeyValueTableDbHelper = mock<IKeyValueTableDbHelper>()
    private val secondKeyValueTableDbHelper = mock<IKeyValueTableDbHelper>()
    private val servicePreferencesDbStorageForMigration = mock<IKeyValueTableDbHelper>()
    private val databaseStorageFactory = mock<DatabaseStorageFactory> {
        on { getPreferencesDbHelper(firstComponentId) } doReturn firstKeyValueTableDbHelper
        on { getPreferencesDbHelper(secondComponentId) } doReturn secondKeyValueTableDbHelper
        on { preferencesDbHelperForServiceMigration } doReturn servicePreferencesDbStorageForMigration
    }

    private lateinit var context: Context
    private lateinit var storage: VitalDataProviderStorage

    @Before
    fun setUp() {
        context = TestUtils.createMockedContext()
        whenever(DatabaseStorageFactory.getInstance(context)).thenReturn(databaseStorageFactory)
        storage = VitalDataProviderStorage(context)
    }

    @Test
    fun commonDataProvider() {
        val commonDataProvider = storage.commonDataProvider
        val commonDataProviderForMigration = storage.commonDataProviderForMigration
        assertThat(commonDataProvider).isNotNull.isInstanceOf(VitalCommonDataProvider::class.java)
        assertThat(commonDataProviderForMigration).isNotNull.isInstanceOf(VitalCommonDataProvider::class.java)
        assertThat(storage.commonDataProvider).isSameAs(commonDataProvider)
        assertThat(vitalCommonDataProviderMockedRule.constructionMock.constructed()).hasSize(2)
        assertThat(commonDataProvider).isEqualTo(vitalCommonDataProviderMockedRule.constructionMock.constructed()[0])
        assertThat(vitalCommonDataProviderMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                GlobalServiceLocator.getInstance().servicePreferences,
                fileVitalDataSourceMockedRule.constructionMock.constructed()[0],
                prefereceServiceDbStorageMockedRule.constructionMock.constructed()[0],
                fileVitalDataSourceMockedRule.constructionMock.constructed()[0]
            )
        assertThat(prefereceServiceDbStorageMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(servicePreferencesDbStorageForMigration)
        assertThat(fileVitalDataSourceMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context, "appmetrica_vital.dat")
    }

    @Test
    fun componentDataProvider() {
        val firstComponentDataProvider = storage.getComponentDataProvider(firstComponentId)
        assertThat(firstComponentDataProvider).isNotNull.isInstanceOf(VitalComponentDataProvider::class.java)
        val secondComponentDataProvider = storage.getComponentDataProvider(secondComponentId)
        assertThat(secondComponentDataProvider).isNotNull.isInstanceOf(VitalComponentDataProvider::class.java)
        assertThat(storage.getComponentDataProvider(firstComponentId)).isSameAs(firstComponentDataProvider)
        assertThat(storage.getComponentDataProvider(secondComponentId)).isSameAs(secondComponentDataProvider)

        //Every component data provider creates once
        assertThat(vitalComponentDataProviderMockedRule.constructionMock.constructed()).hasSize(2)
        assertThat(firstComponentDataProvider)
            .isEqualTo(vitalComponentDataProviderMockedRule.constructionMock.constructed()[0])
        assertThat(secondComponentDataProvider)
            .isEqualTo(vitalComponentDataProviderMockedRule.constructionMock.constructed()[1])

        //Check vital components data provider creation arguments
        assertThat(vitalComponentDataProviderMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                //First instance for first main api key
                preferenceComponentDbStorageMockedRule.constructionMock.constructed()[0],
                compositeFileVitalDataSourceMockedConstructionRule.constructionMock.constructed()[0],
                "$firstComponentId",
                //Second instance for second non main api key
                preferenceComponentDbStorageMockedRule.constructionMock.constructed()[1],
                // First for common; two next for first main api key
                fileVitalDataSourceMockedRule.constructionMock.constructed()[3],
                "$secondComponentId",
            )

        assertThat(preferenceComponentDbStorageMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(firstKeyValueTableDbHelper, secondKeyValueTableDbHelper)

        assertThat(compositeFileVitalDataSourceMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(compositeFileVitalDataSourceMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(listOf(
                "appmetrica_vital_$firstApiKey.dat" to fileVitalDataSourceMockedRule.constructionMock.constructed()[1],
                "appmetrica_vital_main.dat" to fileVitalDataSourceMockedRule.constructionMock.constructed()[2]
            ))

        assertThat(fileVitalDataSourceMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context, "appmetrica_vital.dat",
                context, "appmetrica_vital_$firstApiKey.dat",
                context, "appmetrica_vital_main.dat",
                context, "appmetrica_vital_$secondApiKey.dat",
            )
    }
}
