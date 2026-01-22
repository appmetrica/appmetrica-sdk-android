package io.appmetrica.analytics.impl.db

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.DatabaseScriptsHolder.ServiceDatabaseCreateScript
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceDatabaseCreateScriptTest : CommonTest() {

    private val database = mock<SQLiteDatabase>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val moduleServiceDatabaseFirstTableCreateScript = "Script #1"

    private val firstTableDescription = mock<TableDescription> {
        on { createTableScript } doReturn moduleServiceDatabaseFirstTableCreateScript
    }

    private val moduleServiceDatabaseSecondTableCreateScript = "Script #2"

    private val secondTableDescription = mock<TableDescription> {
        on { createTableScript } doReturn moduleServiceDatabaseSecondTableCreateScript
    }

    private val moduleServiceDatabase = mock<ModuleServicesDatabase> {
        on { tables } doReturn listOf(firstTableDescription, secondTableDescription)
    }

    private val serviceDatabaseCreateScript by setUp { ServiceDatabaseCreateScript() }

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().modulesController.collectModuleServiceDatabases())
            .thenReturn(listOf(moduleServiceDatabase))
    }

    @Test
    fun runScript() {
        serviceDatabaseCreateScript.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.PreferencesTable.CREATE_TABLE)
            verify(database).execSQL(Constants.BinaryDataTable.CREATE_TABLE)
            verify(database).execSQL(TempCacheTable.CREATE_TABLE)
            verify(database).execSQL(moduleServiceDatabaseFirstTableCreateScript)
            verify(database).execSQL(moduleServiceDatabaseSecondTableCreateScript)
            verifyNoMoreInteractions()
        }
    }
}
