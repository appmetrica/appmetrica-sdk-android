package io.appmetrica.analytics.impl.db.constants.migration

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

internal class ComponentDatabaseUpgradeScriptToV112Test : CommonTest() {

    @get:Rule
    val sessionMigratorMockedConstructionRule =
        MockedConstructionRule(ComponentDatabaseUpgradeScriptToV112.SessionsMigrator::class.java)

    @get:Rule
    val eventsMigratorMockedConstructionRule =
        MockedConstructionRule(ComponentDatabaseUpgradeScriptToV112.EventsMigrator::class.java)

    private val database = mock<SQLiteDatabase>()

    private lateinit var componentDatabaseUpgradeScriptToV112: ComponentDatabaseUpgradeScriptToV112
    private lateinit var sessionMigrator: ComponentDatabaseUpgradeScriptToV112.SessionsMigrator
    private lateinit var eventsMigrator: ComponentDatabaseUpgradeScriptToV112.EventsMigrator

    @Before
    fun setUp() {
        componentDatabaseUpgradeScriptToV112 = ComponentDatabaseUpgradeScriptToV112()
        sessionMigrator = sessionMigrator()
        eventsMigrator = eventMigrator()
    }

    @Test
    fun runScript() {
        componentDatabaseUpgradeScriptToV112.runScript(database)

        inOrder(sessionMigrator, eventsMigrator) {
            verify(sessionMigrator).runScript(database)
            verify(eventsMigrator).runScript(database)
            verifyNoMoreInteractions()
        }
    }

    private fun sessionMigrator(): ComponentDatabaseUpgradeScriptToV112.SessionsMigrator {
        assertThat(sessionMigratorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(sessionMigratorMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return sessionMigratorMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun eventMigrator(): ComponentDatabaseUpgradeScriptToV112.EventsMigrator {
        assertThat(eventsMigratorMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(eventsMigratorMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        return eventsMigratorMockedConstructionRule.constructionMock.constructed().first()
    }
}
