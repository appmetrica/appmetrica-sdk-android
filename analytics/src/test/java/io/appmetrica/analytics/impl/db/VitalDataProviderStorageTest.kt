package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.db.storage.ServiceStorageFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
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

    private val context: Context = mock()

    @get:Rule
    val vitalCommonDataProviderMockedRule = MockedConstructionRule(VitalCommonDataProvider::class.java)

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val fileVitalDataSourceMockedRule = MockedConstructionRule(FileVitalDataSource::class.java)

    @get:Rule
    val vitalComponentDataProviderMockedRule = MockedConstructionRule(VitalComponentDataProvider::class.java)

    @get:Rule
    val preferenceComponentDbStorageMockedRule = MockedConstructionRule(PreferencesComponentDbStorage::class.java)

    @get:Rule
    val preferenceServiceDbStorageMockedRule = MockedConstructionRule(PreferencesServiceDbStorage::class.java)

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
    private val databaseStorageFactory = mock<ServiceStorageFactory> {
        on { getComponentPreferenceDbHelper(context, firstComponentId) } doReturn firstKeyValueTableDbHelper
        on { getComponentPreferenceDbHelper(context, secondComponentId) } doReturn secondKeyValueTableDbHelper
        on { getServicePreferenceDbHelperForMigration(context) } doReturn servicePreferencesDbStorageForMigration
    }
    private lateinit var storage: VitalDataProviderStorage

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().getStorageFactory()).thenReturn(databaseStorageFactory)
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
        assertThat(vitalCommonDataProviderMockedRule.argumentInterceptor.arguments[0])
            .containsExactly(
                GlobalServiceLocator.getInstance().servicePreferences,
                fileVitalDataSourceMockedRule.constructionMock.constructed()[0]
            )
        assertThat(fileVitalDataSourceMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(context, "appmetrica_vital.dat")
    }

    @Test
    fun commonDataProviderForMigration() {
        val commonDataProviderForMigration = storage.commonDataProviderForMigration
        assertThat(commonDataProviderForMigration).isNotNull.isInstanceOf(VitalCommonDataProvider::class.java)
        assertThat(storage.commonDataProviderForMigration).isSameAs(commonDataProviderForMigration)
        assertThat(vitalCommonDataProviderMockedRule.constructionMock.constructed()).hasSize(2)
        assertThat(commonDataProviderForMigration)
            .isEqualTo(vitalCommonDataProviderMockedRule.constructionMock.constructed()[1])
        assertThat(vitalCommonDataProviderMockedRule.argumentInterceptor.arguments[1])
            .containsExactly(
                preferenceServiceDbStorageMockedRule.constructionMock.constructed()[0],
                fileVitalDataSourceMockedRule.constructionMock.constructed()[0]
            )
        assertThat(preferenceServiceDbStorageMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(preferenceServiceDbStorageMockedRule.argumentInterceptor.flatArguments())
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

        // Every component data provider creates once
        assertThat(vitalComponentDataProviderMockedRule.constructionMock.constructed()).hasSize(2)
        assertThat(firstComponentDataProvider)
            .isEqualTo(vitalComponentDataProviderMockedRule.constructionMock.constructed()[0])
        assertThat(secondComponentDataProvider)
            .isEqualTo(vitalComponentDataProviderMockedRule.constructionMock.constructed()[1])

        // Check vital components data provider creation arguments
        assertThat(vitalComponentDataProviderMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                // First instance for first main api key
                preferenceComponentDbStorageMockedRule.constructionMock.constructed()[0],
                compositeFileVitalDataSourceMockedConstructionRule.constructionMock.constructed()[0],
                "$firstComponentId",
                // Second instance for second non main api key
                preferenceComponentDbStorageMockedRule.constructionMock.constructed()[1],
                // First for common; two next for first main api key
                fileVitalDataSourceMockedRule.constructionMock.constructed()[3],
                "$secondComponentId",
            )

        assertThat(compositeFileVitalDataSourceMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(compositeFileVitalDataSourceMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                listOf(
                    "appmetrica_vital_$firstApiKey.dat" to fileVitalDataSourceMockedRule.constructionMock
                        .constructed()[1],
                    "appmetrica_vital_main.dat" to fileVitalDataSourceMockedRule.constructionMock.constructed()[2]
                )
            )

        assertThat(fileVitalDataSourceMockedRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context, "appmetrica_vital.dat",
                context, "appmetrica_vital_$firstApiKey.dat",
                context, "appmetrica_vital_main.dat",
                context, "appmetrica_vital_$secondApiKey.dat",
            )
    }
}
