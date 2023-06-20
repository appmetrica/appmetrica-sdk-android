package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.db.constants.Constants

internal class ServiceDatabaseSimpleNameProvider : DatabaseSimpleNameProvider {

    override val databaseName: String = Constants.SERVICE_MAIN_DATABASE

    override val legacyDatabaseName: String = Constants.OLD_SERVICE_DATABASE
}
