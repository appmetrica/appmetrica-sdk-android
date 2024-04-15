package io.appmetrica.analytics.impl.db.constants.migration

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.impl.db.constants.migrations.ServiceDatabaseUpgradeScriptToV114
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class ServiceDatabaseUpgradeScriptToV114Test : CommonTest() {

    private val database: SQLiteDatabase = mock()

    private val script: ServiceDatabaseUpgradeScriptToV114 by setUp { ServiceDatabaseUpgradeScriptToV114() }

    @Test
    fun runScript() {
        script.runScript(database)
        verify(database).execSQL(TempCacheTable.CREATE_TABLE)
        verifyNoMoreInteractions(database)
    }
}
