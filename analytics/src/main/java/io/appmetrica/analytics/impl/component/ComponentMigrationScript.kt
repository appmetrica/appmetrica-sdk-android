package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.logger.internal.YLogger

internal abstract class ComponentMigrationScript(
    val componentUnit: ComponentUnit,
    protected val tag: String
) {

    fun run(from: Int) {
        YLogger.info(tag, "checkMigration from apiLevel = `$from`")
        if (shouldMigrate(from)) {
            YLogger.info(tag, "Should migrate from apiLevel = `$from`")
            run()
        } else {
            YLogger.info(tag, "Ignore migration from apiLevel = `$from`")
        }
    }
    protected abstract fun shouldMigrate(from: Int): Boolean
    protected abstract fun run()
}
