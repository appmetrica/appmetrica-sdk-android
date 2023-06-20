package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript

class TableDescriptionModel(
    override val tableName: String,
    override val createTableScript: String,
    override val dropTableScript: String,
    override val columnNames: List<String>,
    override val databaseProviderUpgradeScript: Map<Int, DatabaseScript>
) : TableDescription
