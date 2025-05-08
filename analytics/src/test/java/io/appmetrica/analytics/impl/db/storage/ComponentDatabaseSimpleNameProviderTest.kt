package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.component.CommutationComponentId
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.MainReporterComponentId
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID

internal class ComponentDatabaseSimpleNameProviderTest : CommonTest() {

    private val packageName = "some.test.package.name"
    private val apiKey = UUID.randomUUID().toString()

    private lateinit var componentId: ComponentId
    private lateinit var simpleNameProvider: ComponentDatabaseSimpleNameProvider

    @Before
    fun setUp() {
        componentId = ComponentId(packageName, apiKey)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
    }

    @Test
    fun `databaseName for component with apiKey`() {
        assertThat(simpleNameProvider.databaseName).isEqualTo("component_$apiKey.db")
    }

    @Test
    fun `databaseName for component without apiKey`() {
        componentId = ComponentId(packageName, null)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.databaseName).isEqualTo("component_null.db")
    }

    @Test
    fun `databaseName for main reporter component id with api key`() {
        componentId = MainReporterComponentId(packageName, apiKey)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.databaseName).isEqualTo("component_main.db")
    }

    @Test
    fun `databaseName for main reporter component id without api key`() {
        componentId = MainReporterComponentId(packageName, null)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.databaseName).isEqualTo("component_main.db")
    }

    @Test
    fun `databaseName for commutation component id`() {
        componentId = CommutationComponentId(packageName)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.databaseName).isEqualTo("component_null.db")
    }

    @Test
    fun `legacyDatabaseName for component with apiKey`() {
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("db_metrica_${packageName}_$apiKey")
    }

    @Test
    fun `legacyDatabaseName for component without apiKey`() {
        componentId = ComponentId(packageName, null)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("db_metrica_${packageName}_null")
    }

    @Test
    fun `legacyDatabaseName for main reporter componentId with apiKey`() {
        componentId = MainReporterComponentId(packageName, apiKey)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("db_metrica_$packageName")
    }

    @Test
    fun `legacyDatabaseName for main reporter componentId without apiKey`() {
        componentId = MainReporterComponentId(packageName, null)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("db_metrica_$packageName")
    }

    @Test
    fun `legacyDatabaseName for commutation componentId`() {
        componentId = CommutationComponentId(packageName)
        simpleNameProvider = ComponentDatabaseSimpleNameProvider(componentId)
        assertThat(simpleNameProvider.legacyDatabaseName).isEqualTo("db_metrica_${packageName}_null")
    }
}
