package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.db.constants.Constants

internal class ClientDatabaseSimpleNameProvider : DatabaseSimpleNameProvider {

    override val databaseName: String = Constants.CLIENT_MAIN_DATABASE

    override val legacyDatabaseName: String = Constants.OLD_CLIENT_DATABASE
}
