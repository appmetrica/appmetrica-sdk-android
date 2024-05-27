package io.appmetrica.analytics.impl.component

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ComponentMigrationHelper private constructor(componentUnit: ComponentUnit) {

    private val tag = "[ComponentMigrationHelper]"

    class Creator {
        fun create(componentUnit: ComponentUnit): ComponentMigrationHelper {
            return ComponentMigrationHelper(componentUnit)
        }
    }

    private val migrationScripts: List<ComponentMigrationScript> = listOf(
        ComponentMigrationToV113(componentUnit),
    )

    fun migrate(from: Int) {
        this.migrationScripts.forEach {
            DebugLogger.info(tag, "run migration: `${it.javaClass}` from apiLevel = `$from`")
            it.run(from)
        }
    }

    @VisibleForTesting
    fun getMigrationScripts(): List<ComponentMigrationScript> = migrationScripts
}
