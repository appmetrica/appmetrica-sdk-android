package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.TablesManager
import io.appmetrica.analytics.impl.db.connectors.LockedOnFileDBConnector
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.io.File

internal class DatabaseStorageFactoryTest : CommonTest() {

    private val context = mock<Context>()
    private val systemOverwrittenDbDir = mock<File>()
    private val mainDatabaseManager = mock<TablesManager>()
    private val serviceDatabaseManager = mock<TablesManager>()
    private val autoInappDatabaseManager = mock<TablesManager>()
    private val clientDatabaseManager = mock<TablesManager>()

    private val databaseManagerProvider = mock<DatabaseManagerProvider> {
        on { buildComponentDatabaseManager(any()) } doReturn mainDatabaseManager
        on { buildServiceDatabaseManager() } doReturn serviceDatabaseManager
        on { buildAutoInappDatabaseManager() } doReturn autoInappDatabaseManager
        on { buildClientDatabaseManager() } doReturn clientDatabaseManager
    }
    private val firstComponentId = mock<ComponentId>()
    private val secondComponentId = mock<ComponentId>()
    private val firstComponentDbName = "First component db name"
    private val secondComponentDbName = "Second component db name"
    private val componentStoragePath = "Component storage path"
    private val serviceStoragePath = "Service storage path"
    private val autoInappStoragePath = "Auto inapp storage path"
    private val clientStoragePath = "Client storage path"

    private val databaseStoragePathProvider = mock<DatabaseStoragePathProvider> {
        on { getPath(eq(context), any<ComponentDatabaseSimpleNameProvider>()) } doReturn componentStoragePath
        on { getPath(eq(context), any<ServiceDatabaseSimpleNameProvider>()) } doReturn serviceStoragePath
        on { getPath(eq(context), any<ClientDatabaseSimpleNameProvider>()) } doReturn clientStoragePath
    }

    @get:Rule
    val constantsMockedStaticRule = staticRule<Constants>()

    @get:Rule
    val databaseStoragePathProviderFactoryMockedConstructionRule =
        constructionRule<DatabaseStoragePathProviderFactory> {
            on { mock.create(any(), any()) } doReturn databaseStoragePathProvider
        }

    @get:Rule
    val componentDatabaseSimpleNameProviderMockedConstructionRule =
        MockedConstructionRule(ComponentDatabaseSimpleNameProvider::class.java) { mock, mockedContext ->
            if (firstComponentId == mockedContext.arguments().first()) {
                whenever(mock.databaseName).thenReturn(firstComponentDbName)
            } else if (secondComponentId == mockedContext.arguments().first()) {
                whenever(mock.databaseName).thenReturn(secondComponentDbName)
            }
        }

    @get:Rule
    val serviceDatabaseSimpleNameProviderMockedConstructionRule = constructionRule<ServiceDatabaseSimpleNameProvider>()

    @get:Rule
    val clientDatabaseSimpleNameProviderMockedConstructionRule = constructionRule<ClientDatabaseSimpleNameProvider>()

    @get:Rule
    val databaseStorageMockedConstructionRule = constructionRule<DatabaseStorage>()

    @get:Rule
    val binaryDbHelperMockedConstructionRule = constructionRule<BinaryDataHelper>()

    @get:Rule
    val binaryDbHelperWrapperMockedConstructionRule = constructionRule<BinaryDataHelperWrapper>()

    @get:Rule
    val tempStorageMockedConstructionRule = constructionRule<TempCacheDbHelper>()

    @get:Rule
    val tempStorageWrapperMockedConstructionRule = constructionRule<TempCacheDbHelperWrapper>()

    @get:Rule
    val keyValueDbHelperMockedConstructionRule = constructionRule<KeyValueTableDbHelper>()

    @get:Rule
    val keyValueDbHelperWrapperMockedConstructionRule = constructionRule<KeyValueTableDbHelperWrapper>()

    @get:Rule
    val simpleDBConnectorMockedConstructionRule = constructionRule<SimpleDBConnector>()

    @get:Rule
    val lockedOnFileDBConnectorMockedConstructionRule = constructionRule<LockedOnFileDBConnector>()

    private lateinit var databaseStorageFactory: DatabaseStorageFactory

    private lateinit var databaseStoragePathProviderFactory: DatabaseStoragePathProviderFactory

    @Before
    fun setUp() {
        whenever(Constants.getDatabaseManagerProvider()).thenReturn(databaseManagerProvider)
        databaseStorageFactory = DatabaseStorageFactory(context, systemOverwrittenDbDir)

        databaseStoragePathProviderFactory = databaseStoragePathProviderFactory()
    }

    @Test
    fun getStorageForComponent() {
        val firstStorageForFirstComponent = databaseStorageFactory.getStorageForComponent(firstComponentId)
        val secondStorageForFirstComponent = databaseStorageFactory.getStorageForComponent(firstComponentId)
        val storageForSecondComponent = databaseStorageFactory.getStorageForComponent(secondComponentId)

        assertThat(firstStorageForFirstComponent)
            .isSameAs(secondStorageForFirstComponent)
            .isSameAs(databaseStorageMockedConstructionRule.constructionMock.constructed().first())
        assertThat(storageForSecondComponent)
            .isSameAs(databaseStorageMockedConstructionRule.constructionMock.constructed()[1])

        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(databaseStorageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context, componentStoragePath, mainDatabaseManager,
                context, componentStoragePath, mainDatabaseManager
            )

        inOrder(databaseStoragePathProvider) {
            verify(databaseStoragePathProvider)
                .getPath(
                    context,
                    componentDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed().first()
                )
            verify(databaseStoragePathProvider)
                .getPath(
                    context,
                    componentDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed().last()
                )
            verifyNoMoreInteractions()
        }

        inOrder(databaseStoragePathProviderFactory) {
            verify(databaseStoragePathProviderFactory).create(firstComponentDbName, false)
            verify(databaseStoragePathProviderFactory).create(secondComponentDbName, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun getPreferencesDbHelper() {
        val firstHelperForFirstComponentId = databaseStorageFactory.getPreferencesDbHelper(firstComponentId)
        val secondHelperForFirstComponentId = databaseStorageFactory.getPreferencesDbHelper(firstComponentId)
        val helperForSecondComponentId = databaseStorageFactory.getPreferencesDbHelper(secondComponentId)

        assertThat(firstHelperForFirstComponentId)
            .isSameAs(secondHelperForFirstComponentId)
            .isSameAs(keyValueDbHelperMockedConstructionRule.constructionMock.constructed()[0])
        assertThat(helperForSecondComponentId)
            .isSameAs(keyValueDbHelperMockedConstructionRule.constructionMock.constructed()[1])

        assertThat(keyValueDbHelperMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(keyValueDbHelperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                databaseStorageMockedConstructionRule.constructionMock.constructed()[0],
                Constants.PreferencesTable.TABLE_NAME,
                databaseStorageMockedConstructionRule.constructionMock.constructed()[1],
                Constants.PreferencesTable.TABLE_NAME
            )

        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(2)
    }

    @Test
    fun getBinaryDbHelperForComponent() {
        val firstHelperForFirstComponentId = databaseStorageFactory.getBinaryDbHelperForComponent(firstComponentId)
        val secondHelperForFirstComponentId = databaseStorageFactory.getBinaryDbHelperForComponent(firstComponentId)
        val helperForSecondComponentId = databaseStorageFactory.getBinaryDbHelperForComponent(secondComponentId)

        assertThat(firstHelperForFirstComponentId)
            .isSameAs(secondHelperForFirstComponentId)
            .isSameAs(binaryDbHelperMockedConstructionRule.constructionMock.constructed().first())
        assertThat(helperForSecondComponentId)
            .isSameAs(binaryDbHelperMockedConstructionRule.constructionMock.constructed()[1])

        assertThat(binaryDbHelperMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(binaryDbHelperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                simpleDBConnectorMockedConstructionRule.constructionMock.constructed()[0],
                Constants.BinaryDataTable.TABLE_NAME,
                simpleDBConnectorMockedConstructionRule.constructionMock.constructed()[1],
                Constants.BinaryDataTable.TABLE_NAME
            )

        assertThat(simpleDBConnectorMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(simpleDBConnectorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                databaseStorageMockedConstructionRule.constructionMock.constructed()[0],
                databaseStorageMockedConstructionRule.constructionMock.constructed()[1]
            )
        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(2)
    }

    @Test
    fun getStorageForService() {
        val first = databaseStorageFactory.storageForService
        val second = databaseStorageFactory.storageForService

        assertThat(first)
            .isSameAs(second)
            .isSameAs(databaseStorageMockedConstructionRule.constructionMock.constructed().first())

        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(databaseStorageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, serviceStoragePath, serviceDatabaseManager)

        verify(databaseStoragePathProviderFactory).create("service", true)
        verify(databaseStoragePathProvider)
            .getPath(
                context,
                serviceDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed().first()
            )
        verifyNoMoreInteractions(databaseStoragePathProviderFactory, databaseStoragePathProvider)

        assertThat(serviceDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(serviceDatabaseSimpleNameProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    @Test
    fun getServiceBinaryDataHelper() {
        val first = databaseStorageFactory.serviceBinaryDataHelper
        val second = databaseStorageFactory.serviceBinaryDataHelper

        assertThat(first)
            .isSameAs(second)
            .isSameAs(binaryDbHelperWrapperMockedConstructionRule.constructionMock.constructed().first())

        assertThat(binaryDbHelperWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(binaryDbHelperWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                StorageType.SERVICE,
                binaryDbHelperMockedConstructionRule.constructionMock.constructed().first()
            )
        checkBinaryDataHelper()
    }

    @Test
    fun getServiceBinaryDataHelperForMigration() {
        val first = databaseStorageFactory.serviceBinaryDataHelper
        val second = databaseStorageFactory.serviceBinaryDataHelper

        assertThat(first)
            .isSameAs(second)
            .isSameAs(binaryDbHelperWrapperMockedConstructionRule.constructionMock.constructed().first())

        checkBinaryDataHelper()
    }

    @Test
    fun getTempCacheStorageForService() {
        val first = databaseStorageFactory.tempCacheStorageForService
        val second = databaseStorageFactory.tempCacheStorageForService

        assertThat(first)
            .isSameAs(second)
            .isSameAs(tempStorageWrapperMockedConstructionRule.constructionMock.constructed().first())

        assertThat(tempStorageWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(tempStorageWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                StorageType.SERVICE,
                tempStorageMockedConstructionRule.constructionMock.constructed().first()
            )

        checkTempCacheDbHelper()
    }

    @Test
    fun getServiceTempCacheDbHelperForMigration() {
        val first = databaseStorageFactory.serviceTempCacheDbHelperForMigration
        val second = databaseStorageFactory.serviceTempCacheDbHelperForMigration

        assertThat(first)
            .isSameAs(second)
            .isSameAs(tempStorageMockedConstructionRule.constructionMock.constructed().first())

        checkTempCacheDbHelper()
    }

    @Test
    fun getPreferencesDbHelperForService() {
        val first = databaseStorageFactory.preferencesDbHelperForService
        val second = databaseStorageFactory.preferencesDbHelperForService

        assertThat(first)
            .isSameAs(second)
            .isSameAs(keyValueDbHelperWrapperMockedConstructionRule.constructionMock.constructed().first())

        assertThat(keyValueDbHelperWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueDbHelperWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                StorageType.SERVICE,
                keyValueDbHelperMockedConstructionRule.constructionMock.constructed().first()
            )
        checkKeyValueDbHelperForPreferences()
    }

    @Test
    fun getPreferencesDbHelperForServiceForMigration() {
        val first = databaseStorageFactory.preferencesDbHelperForServiceMigration
        val second = databaseStorageFactory.preferencesDbHelperForServiceMigration

        assertThat(first)
            .isSameAs(second)
            .isSameAs(keyValueDbHelperMockedConstructionRule.constructionMock.constructed().first())
        checkKeyValueDbHelperForPreferences()
    }

    @Test
    fun getClientDbHelper() {
        val first = databaseStorageFactory.clientDbHelper
        val second = databaseStorageFactory.clientDbHelper

        assertThat(first)
            .isSameAs(second)
            .isSameAs(keyValueDbHelperWrapperMockedConstructionRule.constructionMock.constructed().first())

        assertThat(keyValueDbHelperWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueDbHelperWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                context,
                StorageType.CLIENT,
                keyValueDbHelperMockedConstructionRule.constructionMock.constructed().first()
            )
        checkKeyValueHelperForClient()
    }

    @Test
    fun getClientDbHelperForMigration() {
        val first = databaseStorageFactory.clientDbHelperForMigration
        val second = databaseStorageFactory.clientDbHelperForMigration

        assertThat(first)
            .isSameAs(second)
            .isSameAs(keyValueDbHelperMockedConstructionRule.constructionMock.constructed().first())
        checkKeyValueHelperForClient()
    }

    private fun checkKeyValueHelperForClient() {
        assertThat(keyValueDbHelperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueDbHelperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                Constants.PreferencesTable.TABLE_NAME,
                lockedOnFileDBConnectorMockedConstructionRule.constructionMock.constructed().first()
            )

        assertThat(lockedOnFileDBConnectorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(lockedOnFileDBConnectorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, clientStoragePath, clientDatabaseManager)

        verify(databaseStoragePathProviderFactory).create("client", true)
        verify(databaseStoragePathProvider)
            .getPath(
                context,
                clientDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed().first()
            )
        verifyNoMoreInteractions(databaseStoragePathProvider, databaseStoragePathProvider)

        assertThat(clientDatabaseSimpleNameProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(clientDatabaseSimpleNameProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }

    private fun checkKeyValueDbHelperForPreferences() {
        assertThat(keyValueDbHelperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueDbHelperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                databaseStorageMockedConstructionRule.constructionMock.constructed().first(),
                Constants.PreferencesTable.TABLE_NAME
            )
        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
    }

    private fun checkBinaryDataHelper() {
        assertThat(binaryDbHelperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(binaryDbHelperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                simpleDBConnectorMockedConstructionRule.constructionMock.constructed().first(),
                Constants.BinaryDataTable.TABLE_NAME
            )

        assertThat(simpleDBConnectorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(simpleDBConnectorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseStorageMockedConstructionRule.constructionMock.constructed().first())

        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
    }

    private fun checkTempCacheDbHelper() {
        assertThat(tempStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(tempStorageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                simpleDBConnectorMockedConstructionRule.constructionMock.constructed().first(),
                TempCacheTable.TABLE_NAME
            )

        assertThat(simpleDBConnectorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(simpleDBConnectorMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(databaseStorageMockedConstructionRule.constructionMock.constructed().first())

        assertThat(databaseStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
    }

    private fun databaseStoragePathProviderFactory(): DatabaseStoragePathProviderFactory {
        assertThat(databaseStoragePathProviderFactoryMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(databaseStoragePathProviderFactoryMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(systemOverwrittenDbDir)
        return databaseStoragePathProviderFactoryMockedConstructionRule.constructionMock.constructed().first()
    }
}
