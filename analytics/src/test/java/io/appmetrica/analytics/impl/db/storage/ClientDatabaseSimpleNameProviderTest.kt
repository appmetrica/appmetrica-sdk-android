package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

internal class ClientDatabaseSimpleNameProviderTest : CommonTest() {

    private lateinit var simpleNameProvider: ClientDatabaseSimpleNameProvider

    @Before
    fun setUp() {
        simpleNameProvider = ClientDatabaseSimpleNameProvider()
    }

    @Test
    fun databaseName() {
        assertThat(simpleNameProvider.databaseName).isEqualTo("client.db")
    }

    @Test
    fun legacyDatabaseName() {
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("metrica_client_data.db")
    }

}
