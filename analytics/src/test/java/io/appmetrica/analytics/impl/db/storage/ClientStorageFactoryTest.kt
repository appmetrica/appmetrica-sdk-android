package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.TablesManager
import io.appmetrica.analytics.impl.db.connectors.LockedOnFileDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.File

internal class ClientStorageFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val outerStorageDirectory: File = mock()

    private val databaseStoragePath: String = "databaseStoragePath"

    private val databaseStoragePathProvider: DatabaseStoragePathProvider = mock {
        on { getPath(eq(context), any()) } doReturn databaseStoragePath
    }

    @get:Rule
    val dataStoragePathProviderFactoryRule = constructionRule<DatabaseStoragePathProviderFactory> {
        on { create("client", true) } doReturn databaseStoragePathProvider
    }
    private val databaseStoragePathProviderFactory by dataStoragePathProviderFactoryRule

    @get:Rule
    val keyValueTableDbHelperRule = constructionRule<KeyValueTableDbHelper>()
    private val keyValueTableDbHelper by keyValueTableDbHelperRule

    @get:Rule
    val keyValueTableDbHelperWrapperRule = constructionRule<KeyValueTableDbHelperWrapper>()
    private val keyValueTableDbHelperWrapper by keyValueTableDbHelperWrapperRule

    @get:Rule
    val lockedOnFileDBConnectorRule = constructionRule<LockedOnFileDBConnector>()
    private val lockedOnFileDBConnector by lockedOnFileDBConnectorRule

    private val tablesManager: TablesManager = mock()

    private val databaseManagerProvider = mock<DatabaseManagerProvider> {
        on { buildClientDatabaseManager() } doReturn tablesManager
    }

    @get:Rule
    val dbConstantsRule = staticRule<Constants> {
        on { Constants.getDatabaseManagerProvider() } doReturn databaseManagerProvider
    }

    @get:Rule
    val clientDatabaseSimpleNameProviderRule = constructionRule<ClientDatabaseSimpleNameProvider>()
    private val clientDatabaseSimpleNameProvider by clientDatabaseSimpleNameProviderRule

    private val clientStorageFactory by setUp { ClientStorageFactory(outerStorageDirectory) }

    @Test
    fun getClientDbHelper() {
        assertThat(clientStorageFactory.getClientDbHelper(context))
            .isSameAs(clientStorageFactory.getClientDbHelper(context))
            .isSameAs(keyValueTableDbHelperWrapper)

        assertThat(keyValueTableDbHelperWrapperRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueTableDbHelperWrapperRule.argumentInterceptor.flatArguments())
            .containsExactly(context, StorageType.CLIENT, keyValueTableDbHelper)

        assertThat(keyValueTableDbHelperRule.constructionMock.constructed()).hasSize(1)
        assertThat(keyValueTableDbHelperRule.argumentInterceptor.flatArguments())
            .containsExactly("preferences", lockedOnFileDBConnector)

        assertThat(lockedOnFileDBConnectorRule.constructionMock.constructed()).hasSize(1)
        assertThat(lockedOnFileDBConnectorRule.argumentInterceptor.flatArguments())
            .containsExactly(context, databaseStoragePath, tablesManager)

        verify(databaseStoragePathProvider).getPath(context, clientDatabaseSimpleNameProvider)
    }

    @Test
    fun getClientDbHelperForMigration() {
        assertThat(clientStorageFactory.getClientDbHelperForMigration(context))
            .isSameAs(clientStorageFactory.getClientDbHelperForMigration(context))
            .isSameAs(keyValueTableDbHelper)
    }
}
