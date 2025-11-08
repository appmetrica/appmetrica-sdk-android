package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.TablesManager
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
internal class ServiceStorageFactoryTest : CommonTest() {

    private val context = mock<Context>()

    private val outerStorageDirectory = mock<File>()

    private val firstComponentKey = "Key#1"
    private val secondComponentKey = "Key#2"
    private val firstComponentId: ComponentId = mock()
    private val secondComponentId: ComponentId = mock()
    private val firstComponentNameProvider: DatabaseSimpleNameProvider = mock()
    private val secondComponentNameProvider: DatabaseSimpleNameProvider = mock()
    private val firstComponentTablesManager: TablesManager = mock()
    private val secondComponentTablesManager: TablesManager = mock()
    private val databaseStoragePath = "databaseStoragePath"
    private val firstComponentLegacyDatabaseStoragePath = "firstComponentLegacyDatabaseStoragePath"
    private val secondComponentLegacyDatabaseStoragePath = "secondComponentLegacyDatabaseStoragePath"
    private val firstComponentDatabaseStoragePath = "firstComponentDatabaseStoragePath"
    private val secondComponentDatabaseStoragePath = "secondComponentDatabaseStoragePath"
    private val firstComponentDatabaseName = "Component database #1"
    private val secondComponentDatabaseName = "Component database #2"
    private val databaseStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any()) } doReturn databaseStoragePath
    }

    private val firstComponentLegacyStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any()) } doReturn firstComponentLegacyDatabaseStoragePath
    }

    private val secondComponentLegacyStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any()) } doReturn secondComponentLegacyDatabaseStoragePath
    }

    private val firstComponentDatabaseStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any()) } doReturn firstComponentDatabaseStoragePath
    }

    private val secondComponentDatabaseStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any()) } doReturn secondComponentDatabaseStoragePath
    }

    @get:Rule
    val databaseStoragePathProviderFactoryRule = constructionRule<DatabaseStoragePathProviderFactory> {
        on { create("service", true) } doReturn databaseStoragePathProvider
        on { create(firstComponentKey, false) } doReturn firstComponentLegacyStoragePathProvider
        on { create(secondComponentKey, false) } doReturn secondComponentLegacyStoragePathProvider
        on { create(firstComponentDatabaseName, false) } doReturn firstComponentDatabaseStoragePathProvider
        on { create(secondComponentDatabaseName, false) } doReturn secondComponentDatabaseStoragePathProvider
    }

    @get:Rule
    val componentDatabaseSimpleNameProviderRule = MockedConstructionRule(
        ComponentDatabaseSimpleNameProvider::class.java
    ) { mock, context ->
        val componentId = context!!.arguments().first()
        val databaseName = when (componentId) {
            firstComponentId -> firstComponentDatabaseName
            secondComponentId -> secondComponentDatabaseName
            else -> error("Unexpected component id: $componentId")
        }
        whenever(mock!!.databaseName).thenReturn(databaseName)
    }

    @get:Rule
    val databaseStorageRule = constructionRule<DatabaseStorage>()
    private val databaseStorage by databaseStorageRule

    @get:Rule
    val serviceDatabaseSimpleNameProviderRule = constructionRule<ServiceDatabaseSimpleNameProvider>()
    private val serviceDatabaseSimpleNameProvider by serviceDatabaseSimpleNameProviderRule

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val serviceDatabaseManager: TablesManager = mock()

    private val databaseManagerProvider = mock<DatabaseManagerProvider> {
        on { buildServiceDatabaseManager() } doReturn serviceDatabaseManager
        on { buildComponentDatabaseManager(firstComponentId) } doReturn firstComponentTablesManager
        on { buildComponentDatabaseManager(secondComponentId) } doReturn secondComponentTablesManager
    }

    @get:Rule
    val dbConstantsRule = staticRule<Constants> {
        on { Constants.getDatabaseManagerProvider() } doReturn databaseManagerProvider
    }

    @get:Rule
    val binaryDataHelperRule = constructionRule<BinaryDataHelper>()
    private val binaryDataHelper by binaryDataHelperRule

    @get:Rule
    val binaryDataHelperWrapperRule = constructionRule<BinaryDataHelperWrapper>()
    private val binaryDataHelperWrapper by binaryDataHelperWrapperRule

    @get:Rule
    val simpleDbConnectorRule = constructionRule<SimpleDBConnector>()
    private val simpleDbConnector by simpleDbConnectorRule

    @get:Rule
    val keyValueTableDbHelperWrapperRule = constructionRule<KeyValueTableDbHelperWrapper>()
    private val keyValueTableDbHelperWrapper by keyValueTableDbHelperWrapperRule

    @get:Rule
    val keyValueTableDbHelperRule = constructionRule<KeyValueTableDbHelper>()
    private val keyValueTableDbHelper by keyValueTableDbHelperRule

    @get:Rule
    val tempCacheDbHelperRule = constructionRule<TempCacheDbHelper>()
    private val tempCacheDbHelper by tempCacheDbHelperRule

    @get:Rule
    val tempCacheDbHelperWrapperRule = constructionRule<TempCacheDbHelperWrapper>()
    private val tempCacheDbHelperWrapper by tempCacheDbHelperWrapperRule

    private val serviceStorageFactory by setUp { ServiceStorageFactory(outerStorageDirectory) }

    @Test
    fun getStorageForService() {
        assertThat(serviceStorageFactory.getStorageForService(context))
            .isSameAs(serviceStorageFactory.getStorageForService(context))
            .isSameAs(databaseStorage)

        assertThat(databaseStorageRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStorageRule.argumentInterceptor.flatArguments())
            .containsExactly(context, databaseStoragePath, serviceDatabaseManager)

        assertThat(databaseStoragePathProviderFactoryRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStoragePathProviderFactoryRule.argumentInterceptor.flatArguments())
            .containsExactly(outerStorageDirectory)
        verify(databaseStoragePathProvider).getPath(context, serviceDatabaseSimpleNameProvider)

        assertThat(serviceDatabaseSimpleNameProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(serviceDatabaseSimpleNameProviderRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun getServiceBinaryDataHelperForMigration() {
        assertThat(serviceStorageFactory.getServiceBinaryDataHelperForMigration(context))
            .isSameAs(serviceStorageFactory.getServiceBinaryDataHelperForMigration(context))
            .isSameAs(binaryDataHelper)

        assertThat(binaryDataHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(binaryDataHelperRule.argumentInterceptor.flatArguments())
            .containsExactly(simpleDbConnector, "binary_data")

        assertThat(simpleDbConnectorRule.constructionMock.constructed()).hasSize(1)
        assertThat(simpleDbConnectorRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseStorage)
    }

    @Test
    fun getServiceBinaryDataHelper() {
        assertThat(serviceStorageFactory.getServiceBinaryDataHelper(context))
            .isSameAs(serviceStorageFactory.getServiceBinaryDataHelper(context))
            .isSameAs(binaryDataHelperWrapper)

        assertThat(binaryDataHelperWrapperRule.constructionMock.constructed()).hasSize(1)
        assertThat(binaryDataHelperWrapperRule.argumentInterceptor.flatArguments())
            .containsExactly(context, StorageType.SERVICE, binaryDataHelper)
    }

    @Test
    fun getServicePreferenceDbHelperForMigration() {
        assertThat(serviceStorageFactory.getServicePreferenceDbHelperForMigration(context))
            .isSameAs(serviceStorageFactory.getServicePreferenceDbHelperForMigration(context))
            .isSameAs(keyValueTableDbHelper)

        assertThat(keyValueTableDbHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueTableDbHelperRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseStorage, "preferences")
    }

    @Test
    fun getServicePreferenceDbHelper() {
        assertThat(serviceStorageFactory.getServicePreferenceDbHelper(context))
            .isSameAs(serviceStorageFactory.getServicePreferenceDbHelper(context))
            .isSameAs(keyValueTableDbHelperWrapper)

        assertThat(keyValueTableDbHelperWrapperRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueTableDbHelperWrapperRule.argumentInterceptor.flatArguments())
            .containsExactly(context, StorageType.SERVICE, keyValueTableDbHelper)
    }

    @Test
    fun getServiceTempCacheStorageForMigration() {
        assertThat(serviceStorageFactory.getServiceTempCacheStorageForMigration(context))
            .isSameAs(serviceStorageFactory.getServiceTempCacheStorageForMigration(context))
            .isSameAs(tempCacheDbHelper)

        assertThat(tempCacheDbHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(tempCacheDbHelperRule.argumentInterceptor.flatArguments())
            .containsExactly(simpleDbConnector, "temp_cache")

        assertThat(simpleDbConnectorRule.constructionMock.constructed()).hasSize(1)
        assertThat(simpleDbConnectorRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseStorage)
    }

    @Test
    fun getServiceTempCacheDbHelper() {
        assertThat(serviceStorageFactory.getServiceTempCacheStorage(context))
            .isSameAs(serviceStorageFactory.getServiceTempCacheStorage(context))
            .isSameAs(tempCacheDbHelperWrapper)

        assertThat(tempCacheDbHelperWrapperRule.constructionMock.constructed()).hasSize(1)
        assertThat(tempCacheDbHelperWrapperRule.argumentInterceptor.flatArguments())
            .containsExactly(context, StorageType.SERVICE, tempCacheDbHelper)
    }

    @Test
    fun createComponentLegacyStorageForMigration() {
        val firstStorage = serviceStorageFactory.createComponentLegacyStorageForMigration(
            context,
            firstComponentKey,
            firstComponentNameProvider,
            firstComponentTablesManager
        )
        val oneMoreFirstStorage = serviceStorageFactory.createComponentLegacyStorageForMigration(
            context,
            firstComponentKey,
            firstComponentNameProvider,
            firstComponentTablesManager
        )
        val secondStorage = serviceStorageFactory.createComponentLegacyStorageForMigration(
            context,
            secondComponentKey,
            secondComponentNameProvider,
            secondComponentTablesManager
        )

        assertThat(firstStorage)
            .isNotSameAs(secondStorage)
            .isNotSameAs(oneMoreFirstStorage)

        assertThat(firstStorage).isEqualTo(databaseStorageRule.constructionMock.constructed()[0])
        assertThat(oneMoreFirstStorage).isEqualTo(databaseStorageRule.constructionMock.constructed()[1])
        assertThat(secondStorage).isEqualTo(databaseStorageRule.constructionMock.constructed()[2])

        assertThat(databaseStorageRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(context, firstComponentLegacyDatabaseStoragePath, firstComponentTablesManager),
                listOf(context, firstComponentLegacyDatabaseStoragePath, firstComponentTablesManager),
                listOf(context, secondComponentLegacyDatabaseStoragePath, secondComponentTablesManager)
            )

        verify(firstComponentLegacyStoragePathProvider, times(2)).getPath(context, firstComponentNameProvider)
        verify(secondComponentLegacyStoragePathProvider).getPath(context, secondComponentNameProvider)
    }

    @Test
    fun getComponentStorage() {
        val firstStorage = serviceStorageFactory.getComponentStorage(context, firstComponentId)
        val oneMoreFirstStorage = serviceStorageFactory.getComponentStorage(context, firstComponentId)
        val secondStorage = serviceStorageFactory.getComponentStorage(context, secondComponentId)

        assertThat(firstStorage)
            .isSameAs(oneMoreFirstStorage)
            .isNotSameAs(secondStorage)
            .isSameAs(databaseStorageRule.constructionMock.constructed()[0])

        assertThat(secondStorage).isEqualTo(databaseStorageRule.constructionMock.constructed()[1])

        assertThat(databaseStorageRule.constructionMock.constructed()).hasSize(2)
        assertThat(databaseStorageRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(context, firstComponentDatabaseStoragePath, firstComponentTablesManager),
                listOf(context, secondComponentDatabaseStoragePath, secondComponentTablesManager)
            )
    }

    @Test
    fun getComponentPreferenceDbHelper() {
        val firstComponentPreferenceHelper =
            serviceStorageFactory.getComponentPreferenceDbHelper(context, firstComponentId)
        val oneMoreFirstComponentPreferenceHelper =
            serviceStorageFactory.getComponentPreferenceDbHelper(context, firstComponentId)
        val secondComponentPreferenceHelper =
            serviceStorageFactory.getComponentPreferenceDbHelper(context, secondComponentId)

        assertThat(firstComponentPreferenceHelper)
            .isSameAs(oneMoreFirstComponentPreferenceHelper)
            .isNotSameAs(secondComponentPreferenceHelper)
            .isSameAs(keyValueTableDbHelperRule.constructionMock.constructed()[0])

        assertThat(secondComponentPreferenceHelper)
            .isSameAs(keyValueTableDbHelperRule.constructionMock.constructed()[1])

        assertThat(keyValueTableDbHelperRule.constructionMock.constructed()).hasSize(2)
        assertThat(keyValueTableDbHelperRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(databaseStorageRule.constructionMock.constructed()[0], "preferences"),
                listOf(databaseStorageRule.constructionMock.constructed()[1], "preferences")
            )
    }

    @Test
    fun getComponentBinaryDataHelper() {
        val firstComponentBinaryDataHelper =
            serviceStorageFactory.getComponentBinaryDataHelper(context, firstComponentId)
        val oneMoreFirstComponentBinaryDataHelper =
            serviceStorageFactory.getComponentBinaryDataHelper(context, firstComponentId)
        val secondComponentBinaryDataHelper =
            serviceStorageFactory.getComponentBinaryDataHelper(context, secondComponentId)

        assertThat(firstComponentBinaryDataHelper)
            .isSameAs(oneMoreFirstComponentBinaryDataHelper)
            .isNotSameAs(secondComponentBinaryDataHelper)
            .isSameAs(binaryDataHelperRule.constructionMock.constructed()[0])

        assertThat(secondComponentBinaryDataHelper)
            .isSameAs(binaryDataHelperRule.constructionMock.constructed()[1])

        assertThat(binaryDataHelperRule.constructionMock.constructed()).hasSize(2)
        assertThat(binaryDataHelperRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(simpleDbConnectorRule.constructionMock.constructed()[0], "binary_data"),
                listOf(simpleDbConnectorRule.constructionMock.constructed()[1], "binary_data")
            )

        assertThat(simpleDbConnectorRule.constructionMock.constructed()).hasSize(2)
        assertThat(simpleDbConnectorRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(databaseStorageRule.constructionMock.constructed()[0]),
                listOf(databaseStorageRule.constructionMock.constructed()[1])
            )
    }
}
