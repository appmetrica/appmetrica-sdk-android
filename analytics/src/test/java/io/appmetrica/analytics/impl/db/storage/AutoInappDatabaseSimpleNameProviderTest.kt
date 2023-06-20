package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class AutoInappDatabaseSimpleNameProviderTest : CommonTest() {

    private lateinit var nameProvider: AutoInappDatabaseSimpleNameProvider

    @Before
    fun setUp() {
        nameProvider = AutoInappDatabaseSimpleNameProvider()
    }

    @Test
    fun databaseName() {
        assertThat(nameProvider.databaseName).isEqualTo("auto_inapp.db")
    }

    @Test
    fun legacyDatabaseName() {
        assertThat(nameProvider.legacyDatabaseName).isEqualTo("metrica_aip.db")
    }
}
