package io.appmetrica.analytics.impl.db

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.DatabaseScriptsHolder.ServiceDatabaseDropScript
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
internal class ServiceDatabaseDropScriptTest : CommonTest() {

    private val database = mock<SQLiteDatabase>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val moduleServiceDatabaseFirstTableDropScript = "Script #1"

    private val firstTableDescription = mock<TableDescription> {
        on { dropTableScript } doReturn moduleServiceDatabaseFirstTableDropScript
    }

    private val moduleServiceDatabaseSecondTableDropScript = "Script #2"

    private val secondTableDescription = mock<TableDescription> {
        on { dropTableScript } doReturn moduleServiceDatabaseSecondTableDropScript
    }

    private val moduleServiceDatabase = mock<ModuleServicesDatabase> {
        on { tables } doReturn listOf(firstTableDescription, secondTableDescription)
    }

    private val serviceDatabaseDropScript by setUp { ServiceDatabaseDropScript() }

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().modulesController.collectModuleServiceDatabases())
            .thenReturn(listOf(moduleServiceDatabase))
    }

    @Test
    fun runScript() {
        serviceDatabaseDropScript.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.PreferencesTable.DROP_TABLE)
            verify(database).execSQL(Constants.BinaryDataTable.DROP_TABLE)
            verify(database).execSQL(TempCacheTable.DROP_TABLE)
            verify(database).execSQL(moduleServiceDatabaseFirstTableDropScript)
            verify(database).execSQL(moduleServiceDatabaseSecondTableDropScript)
            verifyNoMoreInteractions()
        }
    }
}
