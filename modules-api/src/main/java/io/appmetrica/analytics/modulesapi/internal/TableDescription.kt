package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript

interface TableDescription {

    val tableName: String

    val createTableScript: String

    val dropTableScript: String

    val columnNames: List<String>

    val databaseProviderUpgradeScript: Map<Int, DatabaseScript>
}
