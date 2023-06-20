package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.constants.Constants

internal class ComponentDatabaseSimpleNameProvider(componentId: ComponentId) : DatabaseSimpleNameProvider {

    override val databaseName: String = Constants.COMPONENT_DB_PATTERN.format(
        if (componentId.isMain) {
            "main"
        } else {
            componentId.apiKey
        }
    )

    override val legacyDatabaseName: String = Constants.OLD_COMPONENT_DATABASE_PREFIX + componentId.toString()
}
