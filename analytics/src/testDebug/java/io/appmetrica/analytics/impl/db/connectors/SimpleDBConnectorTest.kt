package io.appmetrica.analytics.impl.db.connectors

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SimpleDBConnectorTest : CommonTest() {

    private val storage: DatabaseStorage = mock()
    private val db: SQLiteDatabase = mock()

    private lateinit var connector: SimpleDBConnector

    @Before
    fun setUp() {
        connector = SimpleDBConnector(storage)
    }

    @Test
    fun `openDb returns database when successful`() {
        whenever(storage.writableDatabase).thenReturn(db)

        val result = connector.openDb()

        assertThat(result).isEqualTo(db)
    }

    @Test
    fun `openDb returns null when exception thrown`() {
        whenever(storage.writableDatabase).doThrow(RuntimeException("Test exception"))

        val result = connector.openDb()

        assertThat(result).isNull()
    }

    @Test
    fun `closeDb do nothing`() {
        connector.closeDb(db)

        verify(db, never()).close()
    }
}
