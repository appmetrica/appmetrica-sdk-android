package io.appmetrica.analytics.impl.db.constants.migration

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ClientDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ClientDatabaseUpgradeScriptToV112Test : CommonTest() {

    private val database = mock<SQLiteDatabase>()

    private lateinit var clientDatabaseUpgradeScriptToV112: ClientDatabaseUpgradeScriptToV112

    @Before
    fun setUp() {
        clientDatabaseUpgradeScriptToV112 = ClientDatabaseUpgradeScriptToV112()
    }

    @Test
    fun runScript() {
        clientDatabaseUpgradeScriptToV112.runScript(database)
        verify(database).delete(
            Constants.PreferencesTable.TABLE_NAME,
            Constants.PreferencesTable.DELETE_WHERE_KEY,
            arrayOf("NEXT_STARTUP_TIME")
        )
    }
}
