package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.db.constants.Constants

internal class AutoInappDatabaseSimpleNameProvider : DatabaseSimpleNameProvider {

    override val databaseName: String = Constants.AUTO_INAPP_DATABASE

    override val legacyDatabaseName: String = Constants.OLD_AUTO_INAPP_DATABASE
}
