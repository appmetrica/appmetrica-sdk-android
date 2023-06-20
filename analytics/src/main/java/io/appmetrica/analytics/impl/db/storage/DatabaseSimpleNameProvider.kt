package io.appmetrica.analytics.impl.db.storage

internal interface DatabaseSimpleNameProvider {

    val databaseName: String
    val legacyDatabaseName: String
}
